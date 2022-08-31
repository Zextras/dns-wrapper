package io.github.mzanella.dns;

import io.github.mzanella.dns.testutils.DnsResolverDelegator;
import io.github.mzanella.dns.testutils.DnsSimulatorTestContainer;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
class AggregatorDnsTest {

  @Container
  public static DnsSimulatorTestContainer dnsServer = new DnsSimulatorTestContainer.Builder().build();

  private AggregatorDns aggregatorDns;
  private DnsResolverDelegator delegator1;
  private DnsResolverDelegator delegator2;

  @BeforeEach
  public void setup() throws IOException {

    delegator1 = new DnsResolverDelegator(new CustomDns(
        null, Collections.singletonList(new InetSocketAddress(dnsServer.getIp(), dnsServer.getPort())), null
    ));
    delegator2 = new DnsResolverDelegator(new CustomDns(
        null, Collections.singletonList(new InetSocketAddress(dnsServer.getIp(), dnsServer.getPort())), null
    ));
    aggregatorDns = new AggregatorDns(
        hostname -> {throw new UnknownHostException(hostname);},
        hostname -> {throw new UnknownHostException(hostname);},
        hostname -> Collections.emptyList(),
        hostname -> Collections.emptyList(),
        delegator1,
        delegator2,
        hostname -> {throw new RuntimeException();}
    );
  }

  @Test
  public void unknown_host() {
    Assertions.assertThrows(
        UnknownHostException.class,
        () -> aggregatorDns.resolve("reallyreallyrandostuff_kmsdngmdsvnmdnbdvmdnvmdns")
    );
    Assertions.assertEquals(1, delegator1.getCounter());
    Assertions.assertEquals(1, delegator2.getCounter());
  }

  @Test
  public void unknown_host_on_null() {
    Assertions.assertThrows(
        UnknownHostException.class,
        () -> aggregatorDns.resolve(null)
    );
    Assertions.assertEquals(0, delegator1.getCounter());
    Assertions.assertEquals(0, delegator2.getCounter());
  }

  @Test
  public void test() throws UnknownHostException {
    List<InetAddress> resolve = aggregatorDns.resolve("github.com");
    Assertions.assertFalse(resolve.isEmpty());
    Assertions.assertEquals(1, delegator1.getCounter());
    Assertions.assertEquals(1, delegator2.getCounter());
  }
}