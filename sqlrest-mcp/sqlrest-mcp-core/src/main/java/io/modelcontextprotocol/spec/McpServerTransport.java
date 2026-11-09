package io.modelcontextprotocol.spec;

/**
 * Marker interface for the server-side MCP transport.
 *
 * @author Christian Tzolov
 * @author Dariusz Jędrzejczyk
 */
public interface McpServerTransport extends McpTransport {

	/** sqlrest 定制：建立该传输连接时认证通过的客户端令牌，默认无。 */
	default String getClientToken() {
		return null;
	}

}
