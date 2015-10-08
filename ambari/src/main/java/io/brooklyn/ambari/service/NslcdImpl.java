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

import static brooklyn.util.ssh.BashCommands.installExecutable;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;

import brooklyn.entity.basic.Entities;
import brooklyn.entity.effector.EffectorTasks;
import brooklyn.entity.software.SshEffectorTasks;
import brooklyn.management.Task;
import io.brooklyn.ambari.AmbariCluster;
import io.brooklyn.ambari.server.AmbariServer;

public class NslcdImpl extends AbstractExtraService implements Nslcd {

    private static final Logger LOG = LoggerFactory.getLogger(NslcdImpl.class);


    @Override
    public Map<String, Map> getAmbariConfig() {
        return ImmutableMap.<String, Map>of();
    }

    @Override
    public void preClusterDeploy(AmbariCluster ambariCluster) throws ExtraServiceException {
        try {
            LOG.info("{} installing ed on ambari server", this);
            Task<List<?>> rangerServerRequirementsTasks = parallelListenerTask(ambariCluster.getAmbariServers(), new NslcdServerRequirementsFunction());
            Entities.submit(this, rangerServerRequirementsTasks).get();

        } catch (ExecutionException ex) {
            // If something failed, we propagate the exception.
            throw new ExtraServiceException(ex.getMessage());
        } catch (InterruptedException ex) {
            // If something failed, we propagate the exception.
            throw new ExtraServiceException(ex.getMessage());
        }
    }

    @Override
    public void postClusterDeploy(AmbariCluster ambariCluster) throws ExtraServiceException {

    }

    class NslcdServerRequirementsFunction extends AbstractExtraServicesTask<AmbariServer> {

        @Override
        public Task<Integer> sshTaskApply(AmbariServer ambariServer) {
            return SshEffectorTasks
                    .ssh(installExecutable("ed"))
                    .summary("Initialise ed requirements on " + ambariServer.getId())
                    .machine(EffectorTasks.getSshMachine(ambariServer))
                    .newTask()
                    .asTask();
        }
    }

}
