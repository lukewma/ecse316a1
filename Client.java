import java.io.IOException;
import java.lang.*
import java.net.*;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;
import org.apache.commons.cli.*;

public class Client {

	//TODO: change all the int = to Integer.equals or just change the Integer to an int?

	public QType qt = QType.A;
	public int MAX_PACKET = 512;
	private Integer timeout = new Integer(3000);				//default
	private byte[] server = new byte[4];
	private Integer retries = new Integer(5);				//default
	private String address;
	private String name;
	private Integer port = new Integer(53);

	public Main(String args[]){
        Options options = new Options();

        Option timeout = Option.builder("t")
			.required(false)
			.long-opt("timeout")
			.hasArg()
			.numberOfArgs(1)
			.type(Integer)
			.desc("Timeout in seconds")
			.build();
		options.addOption( timeout );

        Option retries = Option.builder("r")
			.required(false)
			.long-opt("retries")
			.hasarg()
			.numberofargs(1)
			.type(Integer)
			.desc("number of tries before error")
			.build();
		options.addOption( retries );

        Option port = Option.builder("p")
			.required(false)
			.long-opt("port")
			.hasarg()
			.numberofargs(1)
			.type(Integer)
			.desc("port number")
			.build();
		options.addOption( port );

		OptionGroup type = new OptionGroup();
		
		Option mx = Option.builder("mx")
			

        try {
            Client testClient = new Client(args);
            testClient.makeRequest();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
	}

	public Client(String args[]){
		try {
            this.parseInputArguments(args);
        } catch (Exception e) {
            throw new IllegalArgumentException("ERROR\tIncorrect input syntax: Please check arguments and try again");
        }
        if (server == null || name == null) {
            throw new IllegalArgumentException("ERROR\tIncorrect input syntax: Server IP and domain name must be provided.");
	}

    public void makeRequest() {
        System.out.println("DnsClient sending request for " + name);
        System.out.println("Server: " + address);
        System.out.println("Request type: " + queryType);
        pollRequest(1);
    }

    private void pollRequest(int retryNumber) {				
        if (retryNumber > maxRetries) {
            System.out.println("ERROR\tMaximum number of retries " + maxRetries+ " exceeded");
            return;
        }

        try {
            //Create Datagram socket and request object(s)
            DatagramSocket socket = new DatagramSocket();
            socket.setSoTimeout(timeout);
            InetAddress inetaddress = InetAddress.getByAddress(server);
            DnsRequest request = new DnsRequest(name, queryType);

            byte[] requestBytes = request.getRequest();
            byte[] responseBytes = new byte[1024];

            DatagramPacket requestPacket = new DatagramPacket(requestBytes, requestBytes.length, inetaddress, port);
            DatagramPacket responsePacket = new DatagramPacket(responseBytes, responseBytes.length);

            //Send packet and time response
            long startTime = System.currentTimeMillis();
            socket.send(requestPacket);
            socket.receive(responsePacket);
            long endTime = System.currentTimeMillis();
            socket.close();

            System.out.println("Response received after " + (endTime - startTime)/1000. + " seconds " + "(" + (retryNumber - 1) + " retries)");

            DnsResponse response = new DnsResponse(responsePacket.getData(), requestBytes.length, queryType);
            response.outputResponse();

        } catch (SocketException e) {
            System.out.println("ERROR\tCould not create socket");
        } catch (UnknownHostException e ) {
            System.out.println("ERROR\tUnknown host");
        } catch (SocketTimeoutException e) {
            System.out.println("ERROR\tSocket Timeout");
            System.out.println("Reattempting request...");
            pollRequest(++retryNumber);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }


}
