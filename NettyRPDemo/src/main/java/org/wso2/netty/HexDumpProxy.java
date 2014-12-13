package org.wso2.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class HexDumpProxy {
	private static int LOCAL_PORT;
	private static String REMOTE_HOST = null;
	private static int REMOTE_PORT;
	private static Properties prop = new Properties();
	private static boolean SECURE_PROXY = false;

	static String TRUST_STORE_LOCATION;
	static String TRUST_STORE_PASSWORD;

	private static String KEY_STORE_LOCATION;
	private static String KEY_STORE_PASSWORD;

	private static boolean SECURE_BACKEND;

	public static void main(String[] args) throws InterruptedException {
		init();

		System.err.println("Proxying *:" + LOCAL_PORT + " to " + REMOTE_HOST + ':' + REMOTE_PORT +
		                   " ...");

		// Configure the bootstrap.
		EventLoopGroup bossGroup = new NioEventLoopGroup(1);
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		try {
			ServerBootstrap b = new ServerBootstrap();
			b.group(bossGroup, workerGroup)
			 .channel(NioServerSocketChannel.class)
			 .option(ChannelOption.SO_KEEPALIVE, true)
			 .handler(new LoggingHandler(LogLevel.INFO))
			 .childHandler(new HexDumpProxyInitializer(REMOTE_HOST, REMOTE_PORT, SECURE_PROXY,
			                                           KEY_STORE_LOCATION, KEY_STORE_PASSWORD,
			                                           SECURE_BACKEND, TRUST_STORE_LOCATION,
			                                           TRUST_STORE_PASSWORD))
			 .childOption(ChannelOption.AUTO_READ, false).bind(LOCAL_PORT).sync().channel()
			 .closeFuture().sync();
		} finally {
			bossGroup.shutdownGracefully();
			workerGroup.shutdownGracefully();
		}

	}

	/**
	 * reads the properties and starts the execution environment.
	 */
	private static void init() {
		InputStream input = null;

		try {

			input = new FileInputStream("src/main/resources/config.properties");

			// load a properties file
			prop.load(input);

			SECURE_PROXY = Boolean.parseBoolean(prop.getProperty("secureProxy"));

			LOCAL_PORT = Integer.parseInt(prop.getProperty("localPort"));

			REMOTE_HOST = String.valueOf(prop.getProperty("remoteHost"));

			REMOTE_PORT = Integer.parseInt(prop.getProperty("remotePort"));

			TRUST_STORE_LOCATION = String.valueOf(prop.getProperty("truststore"));
			TRUST_STORE_PASSWORD = String.valueOf(prop.getProperty("truststorepassword"));

			KEY_STORE_LOCATION = String.valueOf(prop.getProperty("keystore"));
			KEY_STORE_PASSWORD = String.valueOf(prop.getProperty("keystorepassword"));

			SECURE_BACKEND = Boolean.parseBoolean(prop.getProperty("secureBackend"));

		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

}
