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

import brooklyn.config.ConfigKey;
import brooklyn.entity.basic.ConfigKeys;
import brooklyn.entity.proxying.ImplementedBy;
import brooklyn.util.flags.SetFromFlag;

@ImplementedBy(KerberosImpl.class)
public interface Kerberos extends ExtraService {

    @SetFromFlag("kdb.password")
    ConfigKey<String> KDB_PASSWORD = ConfigKeys.newStringConfigKey("kdb.password", "KDB password");

    @SetFromFlag("kdc.adminpassword")
    ConfigKey<String> KDC_ADMIN_PASSWORD = ConfigKeys.newStringConfigKey("kdc.admin.password", "kdc admin password");

    @SetFromFlag("kdc.realm")
    ConfigKey<String> KDC_REALM = ConfigKeys.newStringConfigKey("kdc.realm", "kdc.realm", "HORTOWORKS.COM");

    @SetFromFlag("kdc.admin")
    ConfigKey<String> KDC_ADMIN = ConfigKeys.newStringConfigKey("kdc.admin", "kdc.admin", "admin/admin");    

    @SetFromFlag("kdc.domain")
    ConfigKey<String> KDC_DOMAIN = ConfigKeys.newStringConfigKey("kdc.domain", "kdc.domain", "hortoworks.com");

    
}