/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.brooklyn.ambari.service;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import com.google.api.client.repackaged.com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;

import brooklyn.config.ConfigKey;
import brooklyn.entity.basic.Entities;
import brooklyn.entity.effector.EffectorTasks;
import brooklyn.entity.software.SshEffectorTasks;
import brooklyn.event.AttributeSensor;
import brooklyn.event.basic.Sensors;
import brooklyn.management.Task;
import brooklyn.util.ssh.BashCommands;
import brooklyn.util.text.Identifiers;
import io.brooklyn.ambari.AmbariCluster;
import io.brooklyn.ambari.agent.AmbariAgent;
import io.brooklyn.ambari.server.AmbariServer;

public class KerberosImpl extends AbstractExtraService implements Kerberos {

    @Override
    public Map<String, Map> getAmbariConfig() {

        return ImmutableMap.<String, Map>builder()
                .put("krb5-config", ImmutableMap.builder()
                        .put("kdb.password", getOrGeneratePassword(KDB_PASSWORD))
                        .put("kdc.adminpassword", getOrGeneratePassword(KDC_ADMIN_PASSWORD))
                        .put("KDC_REALM", getConfig(KDC_REALM))
                        .put("kdc.admin", getConfig(KDC_ADMIN))
                        .put("KDC_DOMAIN", getConfig(KDC_DOMAIN))
                        .build())
                .build();
    }

    private String getOrGeneratePassword(ConfigKey<String> passwordConfigKey) {
        String password = getConfig(passwordConfigKey);
        if(Strings.isNullOrEmpty(password)) {
            password = Identifiers.makeRandomId(12);
            AttributeSensor<String> stringAttributeSensor = Sensors.newStringSensor(passwordConfigKey.getName(), passwordConfigKey.getDescription());
            getMutableEntityType().addSensor(stringAttributeSensor);
        }
        return password;
    }


    @Override
    public void preClusterDeploy(AmbariCluster ambariCluster) {

    }

    @Override
    public void postClusterDeploy(AmbariCluster ambariCluster) {
        try {
            Task<List<?>> modifyKrbAclTask = parallelListenerTask(ambariCluster.getAmbariServers(), new AmbariServerDNSLookUpFunction());
            Entities.submit(this, modifyKrbAclTask).get();

            Task<List<?>> configureDNSTask = parallelListenerTask(ambariCluster.getAmbariAgents(), new AmbariAgentRequirementsFunction());
            Entities.submit(this, configureDNSTask).get();

        } catch (InterruptedException | ExecutionException ex) {
            throw new ExtraServiceException(ex.getMessage());
        }
    }

    class AmbariServerDNSLookUpFunction extends AbstractExtraServicesTask<AmbariServer> {

       AmbariServerDNSLookUpFunction() {
           errorKey = "kerberos.acl";
           errorDescription = "Error configuring kadm5.acl";
       }

        @Override
        public Task<Integer> sshTaskApply(AmbariServer ambariServer) {
            Task<Integer> sshTask = SshEffectorTasks
                    .ssh(BashCommands.sudo("echo principal admin/admin >> /var/kerberos/krb5kdc/kadm5.acl"))
                    .summary("Initialise Ranger requirements on " + ambariServer.getId())
                    .machine(EffectorTasks.getSshMachine(ambariServer))
                    .newTask()
                    .asTask();

            return sshTask;
        }
    }


    class AmbariAgentRequirementsFunction extends AbstractExtraServicesTask<AmbariAgent> {

        AmbariAgentRequirementsFunction() {
            errorKey = "kerberos.krb5";
            errorDescription = "Error configuring krb5.conf";
        }

        @Override
        public Task<Integer> sshTaskApply(AmbariAgent ambariAgent) {
            List<String> commands = new LinkedList<String>();
            commands.add(BashCommands.sudo("sed -i 's| dns_lookup_realm = false| dns_lookup_realm = true|g' /etc/krb5.conf"));
            commands.add(BashCommands.sudo("sed -i 's| dns_lookup_kdc = false| dns_lookup_kdc = true|g' /etc/krb5.conf"));
            Task<Integer> sshTask = SshEffectorTasks
                    .ssh(commands)
                    .summary("Change DNS lookup value" + ambariAgent.getId())
                    .machine(EffectorTasks.getSshMachine(ambariAgent))
                    .newTask()
                    .asTask();

            return sshTask;
        }
    }
}
