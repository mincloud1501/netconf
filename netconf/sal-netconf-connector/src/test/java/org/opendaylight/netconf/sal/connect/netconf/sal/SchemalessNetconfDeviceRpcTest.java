package org.opendaylight.netconf.sal.connect.netconf.sal;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.net.InetSocketAddress;
import javax.xml.transform.dom.DOMSource;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.opendaylight.controller.config.util.xml.XmlUtil;
import org.opendaylight.netconf.api.NetconfMessage;
import org.opendaylight.netconf.sal.connect.api.RemoteDeviceCommunicator;
import org.opendaylight.netconf.sal.connect.util.RemoteDeviceId;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

public class SchemalessNetconfDeviceRpcTest {

    @Mock
    private RemoteDeviceCommunicator<NetconfMessage> listener;

    private SchemalessNetconfDeviceRpc deviceRpc;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        RpcResult<NetconfMessage> msg = null;
        ListenableFuture<RpcResult<NetconfMessage>> future = Futures.immediateFuture(msg);
        doReturn(future).when(listener).sendRequest(any(), any());
        deviceRpc = new SchemalessNetconfDeviceRpc(new RemoteDeviceId("device1", InetSocketAddress.createUnresolved("0.0.0.0", 17830)), listener);

    }

    @Test
    public void testInvokeRpc() throws Exception {
        final QName qName = QName.create("urn:ietf:params:xml:ns:netconf:base:1.0", "2011-06-01", "get-config");
        SchemaPath type = SchemaPath.create(true, qName);
        DOMSource src = new DOMSource(XmlUtil.readXmlToDocument("<get-config xmlns=\"dd\">\n" +
                "    <source>\n" +
                "      <running/>\n" +
                "    </source>\n" +
                "    <filter type=\"subtree\">\n" +
                "      <mainroot xmlns=\"urn:dummy:mod-0\">\n" +
                "        <maincontent/>\n" +
                "<choiceList></choiceList>\n" +
                "      </mainroot>\n" +
                "    </filter>\n" +
                "  </get-config>"));
        NormalizedNode<?, ?> input = Builders.anyXmlBuilder()
                .withNodeIdentifier(new YangInstanceIdentifier.NodeIdentifier(qName))
                .withValue(src)
                .build();

        deviceRpc.invokeRpc(type, input);
        ArgumentCaptor<NetconfMessage> msgCaptor = ArgumentCaptor.forClass(NetconfMessage.class);
        ArgumentCaptor<QName> qNameCaptor = ArgumentCaptor.forClass(QName.class);
        verify(listener).sendRequest(msgCaptor.capture(), qNameCaptor.capture());
        System.out.println(XmlUtil.toString(msgCaptor.getValue().getDocument()));
    }
}