package org.apache.brooklyn.ambari.server;

import brooklyn.catalog.Catalog;
import brooklyn.entity.annotation.Effector;
import brooklyn.entity.annotation.EffectorParam;
import brooklyn.entity.basic.SoftwareProcess;
import brooklyn.entity.java.UsesJava;
import brooklyn.entity.proxying.ImplementedBy;
import brooklyn.event.AttributeSensor;
import brooklyn.event.basic.BasicAttributeSensor;

import java.util.List;
import java.util.Set;

@Catalog(name = "Ambari Server", description = "Ambari Server: part of an ambari cluster used to install and monitor a hadoop cluster.")
@ImplementedBy(AmbariServerImpl.class)
public interface AmbariServer extends SoftwareProcess, UsesJava {

    AttributeSensor<List<String>> REGISTERED_HOSTS = new BasicAttributeSensor(
            List.class, "registered.hosts.list", "List of registered agents");

    /**
     * @throws IllegalStateException if times out.
     */
    public void waitForServiceUp();

    @Effector(description = "Creates a cluster")
    public void createCluster(@EffectorParam(name = "Cluster name") String cluster);

    @Effector(description = "Adds a host to a cluster")
    public void addHostToCluster(@EffectorParam(name = "Cluster name") String cluster,
                                 @EffectorParam(name = "Host FQDN") String hostName);

    @Effector(description = "Add a service to a cluster")
    public void addServiceToCluster(@EffectorParam(name = "Cluster name") String cluster,
                                    @EffectorParam(name = "Service name") String service);

    @Effector(description = "Create component")
    public void addComponentToCluster(@EffectorParam(name = "Cluster name") String cluster,
                                      @EffectorParam(name = "Service name") String service,
                                      @EffectorParam(name = "Component name") String component);

    @Effector(description = "Create host component")
    public void createHostComponent(@EffectorParam(name = "Cluster name") String cluster,
                                    @EffectorParam(name = "Host FQDN") String hostName,
                                    @EffectorParam(name = "Component name") String component);

    @Effector(description = "Create and install cluster on hosts with services")
    public void installHDP(@EffectorParam(name = "Cluster Name") String clusterName,
                           @EffectorParam(name = "Blueprint Name") String blueprintName,
                           @EffectorParam(name = "Hosts", description = "List of FQDNs to add to cluster") List<String> hosts,
                           @EffectorParam(name = "Services", description = "List of services to install on cluster") List<String> services);
}
