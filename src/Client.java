import java.io.IOException;
import java.lang.*;
import java.net.*;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;
import org.apache.commons.cli.*;

public class Client {

    public QType qt;
    public int MAX_PACKET = 512;
    private int timeout;
    private byte[] ip = new byte[4];
    private int retries;
    private String address;
    private String dom;
    private int port;

    public void Main(String args[]) throws Exception{

        Options o = this.mkOpt();
        CommandLineParser pars = new DefaultParser();
        HelpFormatter frmt = new HelpFormatter();
        CommandLine cmd;

        try {
            cmd = pars.parse(o, args, true);
            cmdOpt(cmd);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            frmt.printHelp("utility-name", o);
        }

        try {
            this.parseIPDom(cmd.getArgList());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        this.makeReq();

    }
    private void cmdOpt(CommandLine c){
        this.timeout = Integer.parseInt(c.getOptionValue("t", "3"))*1000;
        this.retries = Integer.parseInt(c.getOptionValue("r", "3"));
        this.port = Integer.parseInt(c.getOptionValue("p", "53"));
        this.qt = QType.A;
        if (c.hasOption("mx")) {
            this.qt = QType.MX;
        }
        else if (c.hasOption("ns")) {
            this.qt = QType.NS;
        }
    }

    private Options mkOpt(){
        Options options = new Options();

        Option timeout = Option.builder("t")
                .required(false)
                .longOpt("timeout")
                .hasArg()
                .numberOfArgs(1)
                //.type(Number.class)
                .desc("Timeout in seconds")
                .build();
        options.addOption( timeout );

        Option retries = Option.builder("r")
                .required(false)
                .longOpt("retries")
                .hasArg()
                .numberOfArgs(1)
                //.type(Number.class)
                .desc("number of tries before error")
                .build();
        options.addOption( retries );

        Option port = Option.builder("p")
                .required(false)
                .longOpt("port")
                .hasArg()
                .numberOfArgs(1)
                //.type(Number.class)
                .desc("port number")
                .build();
        options.addOption( port );

        OptionGroup type = new OptionGroup();

        Option mx = Option.builder("mx")
                .longOpt("mail")
                .required(false)
                .hasArg(false)
                .desc("set a mail server query")
                .build();

        Option ns = Option.builder("ns")
                .longOpt("name")
                .required(false)
                .hasArg(false)
                .desc("set a name server query")
                .build();

        Option a = Option.builder("a")
                .longOpt("ip")
                .required(false)
                .hasArg(false)
                .desc("send ip address query (default)")
                .build();

        type.addOption(ns);
        type.addOption(mx);
        type.addOption(a);

        options.addOptionGroup(type);		//use hasOption bool for cases

        //because its not flags we'll just parse the rest of the input normally. CLI makes this too complicated
        //could consider keeping some of this as a stub to generate help info
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

        this.dom = argL.get(argL.size() - 1);
        String[] octets;
        if(argL.get(argL.size() - 2).contains("@")){
            this.address = argL.get(argL.size() - 2).substring(1);
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

    public void makeReq() {
        System.out.println("DnsClient sending request for " + dom);
        System.out.println("Server: " + address);
        System.out.println("Request type: " + qt);
        pollReq(1);
    }

    private void pollReq(int tryNum) {
        if (tryNum > retries) {
            System.out.println("ERROR\tMaximum number of retries " + retries+ " exceeded");
            return;
        }

        try {
            //Create Datagram socket and request object(s)
            DatagramSocket socket = new DatagramSocket();
            socket.setSoTimeout(timeout);
            InetAddress inetaddress = InetAddress.getByAddress(ip);
            Request request = new Request(dom, qt);

            byte[] requestBytes = request.getReq();
            byte[] responseBytes = new byte[1024];

            DatagramPacket requestPacket = new DatagramPacket(requestBytes, requestBytes.length, inetaddress, port);
            DatagramPacket responsePacket = new DatagramPacket(responseBytes, responseBytes.length);

            //Send packet and time response
            long startTime = System.currentTimeMillis();
            socket.send(requestPacket);
            socket.receive(responsePacket);
            long endTime = System.currentTimeMillis();
            socket.close();

            System.out.println("Response received after " + (endTime - startTime)/1000. + " seconds " + "(" + (tryNum - 1) + " retries)");

            //TODO: refactor appropriately
            Response response = new Response(responsePacket.getData(), requestBytes.length, qt);
            response.outputResponse();

        } catch (SocketException e) {
            System.out.println("ERROR\tCould not create socket");
        } catch (UnknownHostException e ) {
            System.out.println("ERROR\tUnknown host");
        } catch (SocketTimeoutException e) {
            System.out.println("ERROR\tSocket Timeout");
            System.out.println("Reattempting request...");
            pollReq(++tryNum);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }


}
