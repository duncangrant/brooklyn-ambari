package io.brooklyn.ambari.server;

import static brooklyn.test.Asserts.assertThat;
import static brooklyn.util.collections.CollectionFunctionals.contains;
import static brooklyn.util.collections.CollectionFunctionals.sizeEquals;

import java.util.List;

import org.apache.http.auth.UsernamePasswordCredentials;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import io.brooklyn.ambari.rest.AmbariRequestInterceptor;
import io.brooklyn.ambari.rest.domain.Components;
import io.brooklyn.ambari.rest.domain.ServiceComponent;
import io.brooklyn.ambari.rest.endpoint.ServiceEndpoint;
import retrofit.RestAdapter;

public class AmbariServerImplTest {

    private AmbariServerImpl ambariServer = new AmbariServerImpl();

    @Test
    public void testEmptyJsonThrows() {
        assertThat(getHostsFromJson("{}"), sizeEquals(0));
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void testNullJsonThrows() {
        assertThat(ambariServer.getHosts().apply(null), sizeEquals(0));
    }

    @Test
    public void testOneHostReturnsSingleItemInList() {
        assertThat(getHostsFromJson(JSON_WITH_ONE_HOST), contains("ip-10-121-18-69.eu-west-1.compute.internal"));
    }

    @Test
    public void testFourHostsReturnsFourItemsInList() {
        assertThat(getHostsFromJson(JSON_WITH_FOUR_HOSTS), contains("ip-10-121-18-69.eu-west-1.compute.internal"));
        assertThat(getHostsFromJson(JSON_WITH_FOUR_HOSTS), contains("ip-10-121-20-75.eu-west-1.compute.internal"));
        assertThat(getHostsFromJson(JSON_WITH_FOUR_HOSTS), contains("ip-10-122-4-179.eu-west-1.compute.internal"));
        assertThat(getHostsFromJson(JSON_WITH_FOUR_HOSTS), contains("ip-10-91-154-171.eu-west-1.compute.internal"));
    }

    private UsernamePasswordCredentials usernamePasswordCredentials = new UsernamePasswordCredentials("admin", "admin");
    @Test
    public void testAmbariComponents() {
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint("http://ec2-54-204-131-80.compute-1.amazonaws.com:8080/")
                .setRequestInterceptor(new AmbariRequestInterceptor(usernamePasswordCredentials))
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .build();

        Components components = restAdapter.create(ServiceEndpoint.class).getComponents("Cluster1");
        Assert.assertEquals(components.getComponents().size(), 28);
    }

    @Test
    public void testAmbariHDFSComponent() {
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint("http://ec2-54-204-131-80.compute-1.amazonaws.com:8080/")
                .setRequestInterceptor(new AmbariRequestInterceptor(usernamePasswordCredentials))
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .build();

        ServiceComponent component = restAdapter.create(ServiceEndpoint.class).getComponent("Cluster1", "DATANODE");
        Assert.assertEquals(component.getComponentInfo().getComponent(), "DATANODE");
        Assert.assertEquals(component.getMetrics().get("boottime"), 4.321734317E9);
    }

    private List<String> getHostsFromJson(String json) {
        return ambariServer.getHosts().apply(getAsJsonObject(json));
    }

    private JsonObject getAsJsonObject(String jsonWithOneHost) {
        return new JsonParser().parse(jsonWithOneHost).getAsJsonObject();
    }

    private static final String JSON_WITH_ONE_HOST = "{\n" +
            "  \"href\" : \"http://ec2-54-228-116-93.eu-west-1.compute.amazonaws.com:8080/api/v1/hosts\",\n" +
            "  \"items\" : [\n" +
            "    {\n" +
            "      \"href\" : \"http://ec2-54-228-116-93.eu-west-1.compute.amazonaws.com:8080/api/v1/hosts/ip-10-121-18-69.eu-west-1.compute.internal\",\n" +
            "      \"Hosts\" : {\n" +
            "        \"host_name\" : \"ip-10-121-18-69.eu-west-1.compute.internal\"\n" +
            "      }\n" +
            "    }\n" +
            "  ]\n" +
            "}";

    private static final String JSON_WITH_FOUR_HOSTS = "{\n" +
            "  \"href\" : \"http://ec2-54-228-116-93.eu-west-1.compute.amazonaws.com:8080/api/v1/hosts\",\n" +
            "  \"items\" : [\n" +
            "    {\n" +
            "      \"href\" : \"http://ec2-54-228-116-93.eu-west-1.compute.amazonaws.com:8080/api/v1/hosts/ip-10-121-18-69.eu-west-1.compute.internal\",\n" +
            "      \"Hosts\" : {\n" +
            "        \"host_name\" : \"ip-10-121-18-69.eu-west-1.compute.internal\"\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"href\" : \"http://ec2-54-228-116-93.eu-west-1.compute.amazonaws.com:8080/api/v1/hosts/ip-10-121-20-75.eu-west-1.compute.internal\",\n" +
            "      \"Hosts\" : {\n" +
            "        \"host_name\" : \"ip-10-121-20-75.eu-west-1.compute.internal\"\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"href\" : \"http://ec2-54-228-116-93.eu-west-1.compute.amazonaws.com:8080/api/v1/hosts/ip-10-122-4-179.eu-west-1.compute.internal\",\n" +
            "      \"Hosts\" : {\n" +
            "        \"host_name\" : \"ip-10-122-4-179.eu-west-1.compute.internal\"\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"href\" : \"http://ec2-54-228-116-93.eu-west-1.compute.amazonaws.com:8080/api/v1/hosts/ip-10-91-154-171.eu-west-1.compute.internal\",\n" +
            "      \"Hosts\" : {\n" +
            "        \"host_name\" : \"ip-10-91-154-171.eu-west-1.compute.internal\"\n" +
            "      }\n" +
            "    }\n" +
            "  ]\n" +
            "}";

}