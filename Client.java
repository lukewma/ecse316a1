import java.io.IOException;
import java.lang.*
import java.net.*;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;
import org.apache.commons.cli.*;

public class Client {

	//TODO: change all the int = to Integer.equals or just change the Integer to an int?

	//TODO: do these defaults work with cli options?
	public QType qt = QType.A;
	public int MAX_PACKET = 512;
	private Integer timeout = new Integer(3000);				//default
	private byte[] ip = new byte[4];
	private Integer retries = new Integer(5);				//default
	private String address;
	private String dom;
	private Integer port = new Integer(53);

	public Main(String args[]) throws Exception{

		//TODO:change cli parse into a single function, change data fields
		Options o = this.mkOpt();
		CommandLineParser pars = new DefaultParser();
		HelpFormatter frmt = new HelpFormatter();
		CommandLine cmd;

        try {
            cmd = pars.parse(o, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
			frmt.printHelp("utility-name", o);
        }

        try {
			this.parseIPDom(args);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		
		this.makeReq();
			
	}

	public Options mkOpt(){

        Options options = new Options();

        Option timeout = Option.builder("t")
			.required(false)
			.long-opt("timeout")
			.hasArg()
			.numberOfArgs(1)
			.type(Number.class)
			.desc("Timeout in seconds")
			.build();
		options.addOption( timeout );

        Option retries = Option.builder("r")
			.required(false)
			.long-opt("retries")
			.hasarg()
			.numberofargs(1)
			.type(Number.class)
			.desc("number of tries before error")
			.build();
		options.addOption( retries );

        Option port = Option.builder("p")
			.required(false)
			.long-opt("port")
			.hasarg()
			.numberofargs(1)
			.type(Number.class)
			.desc("port number")
			.build();
		options.addOption( port );

		OptionGroup type = new OptionGroup();
		
		Option mx = Option.builder("mx")
			.long-opt("mail")
			.required(false)
			.hasarg(false)
			.desc("set a mail server")
			.build();

		Option ns = Option.builder("ns")
			.long-opt("name")
			.required(false)
			.hasarg(false)
			.desc("set a name server")
			.build();

		Option a = Option.builder("a")
			.long-opt("ip")
			.required(false)
			.hasarg(false)
			.desc("send ip address query (default)")
			.build();

		type.addOption(ns);
		type.addOption(mx);
		type.addOption(a);

		options.addOptionGroup(type);		//use hasOption bool for cases

		//because its not flags we'll just parse the rest of the input normally. CLI makes this too complicated
		//Option serv = Option.builder("@")
		//	.required(true)
		//	.hasarg()
		//	.numberOfArgs(4)
		//	.valueSeparator('.')
		//	type(Number[])
		//options.addOption("@")

		//Option domain = Option.builder

		return options;
	}

	private void parseIPDom(String args[]){
		List<String> argL= Arrays.asList(args);

		this.dom = argL[argL.length - 1];
		String[] octets;
		if(argL[argl.length -2].contains("@")){
			this.address = argL[argl.length -2].substring(1);
			octets = address.split("\\.");

			if(octets.length != 4) {
				throw new IPFormatException("ERROR\tIncorrect input syntax: IP must contain 4 numbers between 0 and 255 inclusively, separated by a period (.)");
			}

			for(int i = 0; i < 4; i++){
				int oct = Integer.parseInt(octets[i]);
				if (oct < 0 || oct > 255) { 
					throw new IPFormatException("ERROR\tIncorrect input syntax: IP must contain 4 numbers between 0 and 255 inclusively, separated by a period (.)");
				}
				ip[i] = (byte) oct;
			}
		}
		else throw new MissingArgException("ERROR\tThere must be an IP address preceded by an '@' as the second last argument and a domain name as the last one")
	}

    public void makeRequest() {
        System.out.println("DnsClient sending request for " + dom);
        System.out.println("Server: " + address);
        System.out.println("Request type: " + QType);
        pollRequest(1);
    }

    private void pollRequest(int retryNumber) {				
        if (retryNumber > retries) {
            System.out.println("ERROR\tMaximum number of retries " + retries+ " exceeded");
            return;
        }

        try {
            //Create Datagram socket and request object(s)
            DatagramSocket socket = new DatagramSocket();
            socket.setSoTimeout(timeout);
            InetAddress inetaddress = InetAddress.getByAddress(ip);
            DnsRequest request = new DnsRequest(name, QType);

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

            //TODO: refactor appropriately
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
