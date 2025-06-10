package africa.payaza.routing.core.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder(toBuilder = true)
public class PlatformTenantConnection {
    private String id;
    private String schemaServer;
    private String schemaPort;
    private String schemaName;
    private String schemaUsername;
    private String schemaPassword;
    private Integer poolSize;
    private Integer poolMinimumIdle;
    private Integer poolMaximumPoolSize;


    @Override
    public String toString() {
        return this.schemaName + ":" + this.schemaServer + ":" +
                this.schemaPort; //+ "?currentSchema=" + this.schemaName;
    }
}
