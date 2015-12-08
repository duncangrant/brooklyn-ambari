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
package io.brooklyn.ambari;

import static org.apache.brooklyn.util.ssh.BashCommands.*;

import java.util.List;

import org.apache.brooklyn.api.location.OsDetails;
import org.apache.brooklyn.location.ssh.SshMachineLocation;
import org.apache.brooklyn.util.ssh.BashCommands;

import com.google.common.collect.ImmutableList;


public class AmbariInstallCommands {

    private static final String CENTOS_REPO_LIST_LOCATION = "/etc/yum.repos.d/ambari.repo";
    private static final String CENTOS_7_AMBARI_REPO_LOCATION = "http://public-repo-1.hortonworks.com/ambari/centos7/%s/updates/%s/ambari.repo";
    private static final String CENTOS_6_AMBARI_REPO_LOCATION = "http://public-repo-1.hortonworks.com/ambari/centos6/%s/updates/%s/ambari.repo";
    private static final String CENTOS_5_AMBARI_REPO_LOCATION = "http://public-repo-1.hortonworks.com/ambari/centos5/%s/updates/%s/ambari.repo";

    private static final String SUSE_REPO_LIST_LOCATION = "/etc/zypp/repos.d/ambari.repo";
    private static final String SUSE_AMBARI_REPO_LOCATION = "http://public-repo-1.hortonworks.com/ambari/suse11/%s/updates/%s/ambari.repo";

    private static final String UBUNTU_REPO_LIST_LOCATION = "/etc/apt/sources.list.d/ambari.list";
    private static final String UBUNTU_14_AMBARI_REPO_LOCATION = "http://public-repo-1.hortonworks.com/ambari/ubuntu14/%s/updates/%s/ambari.list";
    private static final String UBUNTU_12_AMBARI_REPO_LOCATION = "http://public-repo-1.hortonworks.com/ambari/ubuntu12/%s/updates/%s/ambari.list";

    private String version;

    public AmbariInstallCommands(String version) {
        this.version = version;
    }

    public String installAmbariRequirements(SshMachineLocation machine) {
        return BashCommands.chainGroup(BashCommands.INSTALL_CURL,
                installExecutable("ntp"),
                BashCommands.alternatives(sudo("service ntpd start"),
                        sudo("service ntp start")),
                createCommandToAddAmbariToRepositoriesList(machine));
    }

    private String createCommandToAddAmbariToRepositoriesList(SshMachineLocation sshMachineLocation) {
        return alternatives(setupAptRepo(sshMachineLocation), setupYumRepo(sshMachineLocation), setupZypperRepo());
    }

    private String setupAptRepo(SshMachineLocation sshMachineLocation) {
        final String osDetailsVersion = getOsVersion(sshMachineLocation);

        String repoUrl;
        if (osDetailsVersion.startsWith("14")) {
            repoUrl = UBUNTU_14_AMBARI_REPO_LOCATION;
        } else {
            repoUrl = UBUNTU_12_AMBARI_REPO_LOCATION;
        }

        return ifExecutableElse1("apt-get", chainGroup(sudo(commandToDownloadUrlAs(String.format(repoUrl, getMajorVersion(), version), UBUNTU_REPO_LIST_LOCATION)),
                sudo("apt-key adv --recv-keys --keyserver keyserver.ubuntu.com B9733A7A07513CAD"),
                sudo("apt-get update")));
    }

    private String setupYumRepo(SshMachineLocation sshMachineLocation) {
        final String osDetailsVersion = getOsVersion(sshMachineLocation);

        String repoUrl;
        if (osDetailsVersion.startsWith("7")) {
            repoUrl = CENTOS_7_AMBARI_REPO_LOCATION;
        } else if (osDetailsVersion.startsWith("6")) {
            repoUrl = CENTOS_6_AMBARI_REPO_LOCATION;
        } else {
            repoUrl = CENTOS_5_AMBARI_REPO_LOCATION;
        }

        return ifExecutableElse1("yum", sudo(commandToDownloadUrlAs(String.format(repoUrl, getMajorVersion(), version), CENTOS_REPO_LIST_LOCATION)));
    }

    private String setupZypperRepo() {
        return ifExecutableElse1("zypper", sudo(commandToDownloadUrlAs(String.format(SUSE_AMBARI_REPO_LOCATION, getMajorVersion(), version), SUSE_REPO_LIST_LOCATION)));
    }

    private String getOsVersion(SshMachineLocation sshMachineLocation) {
        if (sshMachineLocation == null) {
            return "";
        }
        OsDetails osDetails = sshMachineLocation.getOsDetails();
        return osDetails != null ? osDetails.getVersion() : "";
    }

    private String getMajorVersion() {
        return version.charAt(0) + ".x";
    }

    public List<String> setHostname(String fqdn, String hostnameScriptLocation) {
        List<String> commands = ImmutableList.of(
                BashCommands.sudo("echo " +
                        "'#!/bin/sh\n" +
                        "echo " + fqdn +
                        "' > " + hostnameScriptLocation),
                BashCommands.sudo("chmod a+x " + hostnameScriptLocation)
        );
        return commands;
    }

}
