package lab.idioglossia.row.client;

import lab.idioglossia.row.client.tyrus.RowClientConfig;
import lab.idioglossia.row.client.tyrus.TyrusRowWebsocketClient;
import lab.idioglossia.row.client.ws.RowWebsocketSession;
import lab.idioglossia.row.client.ws.SpringRowWebsocketClient;
import lab.idioglossia.row.client.ws.SpringRowWebsocketSession;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

public class SpringRowClientFactory implements RowClientFactory<SpringRowWebsocketSession> {
    private RowClientConfig rowClientConfig;
    private RowHttpClient rowHttpClient;

    public SpringRowClientFactory(RowClientConfig rowClientConfig, @Nullable RowHttpClient rowHttpClient) {
        this.rowClientConfig = rowClientConfig;
        this.rowHttpClient = rowHttpClient;
    }

    public synchronized RowClient getRowClient(String address){
        Assert.notNull(address, "Address cant be null as default client config might not have an address");
        RowClientConfig rowClientConfig = RowClientConfigHelper.clone(this.rowClientConfig);
        rowClientConfig.setAddress(address);
        if(this.rowHttpClient == null){
            return this.getRowClient(rowClientConfig);
        }
        return this.getRowClient(rowClientConfig, this.rowHttpClient);
    }

    public RowClient getRowClient(RowClientConfig rowClientConfig){
        Assert.notNull(rowClientConfig.getAddress(), "Address cant be null");
        return new SpringRowWebsocketClient(rowClientConfig);
    }

    public RowClient getRowClient(RowClientConfig rowClientConfig, RowHttpClient rowHttpClient){
        return new HttpFallbackRowClientDecorator(getRowClient(rowClientConfig), rowHttpClient);
    }

    public RowClientConfig getRowClientConfig(){
        return RowClientConfigHelper.clone(this.rowClientConfig);
    }

}
