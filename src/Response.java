import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

public class Response
{
    byte[] response;
    byte[] ID;
    int size_req;
    QType q_type;
    boolean QR;
    boolean AA;
    boolean TC;
    boolean RD;
    boolean RA;
    int RCODE;
    int QDCOUNT;
    int ANCOUNT;
    int NSCOUNT;
    int ARCOUNT;
    Record[] answers;
    Record[] additionals;

    //construct what a response is actually supposed to have
    public Response(byte[] response, int size_req, QType q_type)
    {
        this.response = response;
        this.q_type = q_type;

        // need to assemble the packet
        // header question answers

        // create header
        // ID, QR, Opcode, aa tc rd ra, z, rcode, qdcount, ancount, nscount, arcount

        // ID
        byte[] ID = new byte[2];
        // assign to the response
        ID[0] = response[0];
        ID[1] = response[1];
        this.ID = ID;

        // we can just follow the dnsprimer instructions for locations of the 1 bit fields
        this.QR = bitSelect(response[2], 7) == 1;
        this.AA = bitSelect(response[2], 2) == 1;
        this.TC = bitSelect(response[2], 1) == 1;
        this.RD = bitSelect(response[2], 0) == 1;
        this.RA = bitSelect(response[3], 7) == 1;
        this.RCODE = response[3] & 0x0f;
        // next for the 2 byte fields, need to convert to legible shorts and insert
        // this means we need 16 bits to represent, so we can construct a short
        QDCOUNT =  bytesToShort(response[4], response[5]);
        ANCOUNT =  bytesToShort(response[6], response[7]);
        NSCOUNT =  bytesToShort(response[8], response[9]);
        ARCOUNT =  bytesToShort(response[10], response[11]);

        // next with the header made, we move onto the question
        // populating with 0s and change depending on q_type

        // checking if its appropriate
        int idx = 12; // indexing begins at 13th byte
        while(this.response[idx] != 0)
        {
            idx++;
        }
        byte b1 = this.response[idx+1];
        byte b2 = this.response[idx+2];
        byte[] check_q_type = {b1, b2};
        QType checker = QType.A;
        // ERROR CHECK THIS
//        if (check_q_type == 1 {
//            checker = QType.A;
//        }
//        else if (check_q_type == (short) 0x0005) {
//            checker = QType.CNAME;
//        }
//        else if (check_q_type == (short) 0x0002) {
//            checker = QType.NS;
//        }
//        else if (check_q_type == (short) 0x000f) {
//            checker = QType.MX;
//        } else {
//            System.out.println("ERROR\tQuery type of response unknown.");
//            checker = QType.OTHER;
//        }


        // move onto answers
        int offset = size_req;
        // answers are all the records so we just go for the amount of records needed and create an array
        answers = new Record[ANCOUNT];
        for(int i = 0; i < ANCOUNT; i++)
        {
            answers[i] = this.parseRecord(offset);
            int bytesUsed = answers[i].getLengthOfByte();
            offset += bytesUsed;
        }

        // we have our answers and need to check for the additional records as decided by the ar
        additionals = new Record[ARCOUNT];
        for(int i = 0; i < ARCOUNT; i++)
        {
            additionals[i] = this.parseRecord(offset);
            int bytesUsed = answers[i].getLengthOfByte();
            offset += bytesUsed;
        }





    }

    private static int bitSelect(byte b, int bit_pos)
    {
        // bit shift the amount of times from a selected byte to find the appropriate location
        return (b >> bit_pos) & 1;
    }

    private static short bytesToShort(byte b1, byte b2)
    {
        short newCombinedShort = (short) (b1 << 8 | (b2 & 0xff));
        return newCombinedShort;
    }

    private Record parseRecord(int answer_index)
    {
        // set default query
        Record record_result = new Record(this.AA);
        int counter = answer_index;
        String alias;

        Resource currentRecordInfo = retrieveAliasInfo(counter);
        alias = currentRecordInfo.alias;
        counter += currentRecordInfo.bs;

        // we can start setting the fields
        // name
        record_result.setName(alias);
        // type
        byte answerType1 = response[counter];
        byte answerType2 = response[counter+1];
        byte[] answer_type = {answerType1, answerType2};
        // determine which type this is
        QType qt = QType.OTHER; // placeholder
        if (answer_type[0] != 0)
        {
           qt = QType.OTHER;
        }
        else
        {
           if (answer_type[1] == 1)
           {
              qt = QType.A;
           }
           else if (answer_type[1] == 2)
           {
              qt = QType.NS;
           }
           else if (answer_type[1] == 15)
           {
              qt =  QType.MX;
           }
           else if (answer_type[1] == 5)
           {
              qt = QType.CNAME;
           }
           else
           {
              qt = QType.OTHER;
           }
        }
        // set accordingly
        record_result.setQType(qt);
        counter += 2;
        // move on

        // class
        byte answerClass1 = response[counter];
        byte answerClass2 = response[counter+1];
        byte[] answer_class = {answerClass1, answerClass2};
        //TODO error check for 0 != 0 and 1 != 1
        record_result.setQClass(answer_class);
        // move on
        counter += 2;

        // time to live
        byte ttl0 = response[counter];
        byte ttl1 = response[counter+1];
        byte ttl2 = response[counter+2];
        byte ttl3 = response[counter+3];
        byte [] answer_ttl = {ttl0, ttl1, ttl2, ttl3};
        // need in int
        ByteBuffer wrapping  = ByteBuffer.wrap(answer_ttl);
        int answer_ttl_int = wrapping.get();
        record_result.setTTL(answer_ttl_int);
        //move on
        counter += 4;

        // rdlength
        byte rd1 = response[counter];
        byte rd2 = response[counter+1];
        byte[] answer_rdlength = {rd1, rd2};
        // again need in int
        wrapping  = ByteBuffer.wrap(answer_rdlength);
        int answer_rdlength_int = wrapping.get();
        record_result.setRDLength(answer_rdlength_int);
        // move on
        counter += 2;

        // finally, time to set the data in relation to what type it is with the alias
        switch(record_result.getQType())
        {
            // the 4 cases and other if none or error
            // depending on which we can finally update its appropriate alias for the data appropriately
            case A:
                // assemble the ip address
                byte addr0 = response[counter];
                byte addr1 = response[counter+1];
                byte addr2 = response[counter+2];
                byte addr3 = response[counter+3];
                byte[] answer_address = {addr0, addr1, addr2, addr3};
                String address_string = "";
                // utilize the inet address to convert into a usable address with these bytes
                try
                {
                    InetAddress inetaddress = InetAddress.getByAddress(answer_address); // need to surround in try catch for error handling
                    address_string = inetaddress.toString().substring(1); // TODO MIGHT HAVE TO MOVE EVERYTHING IN HERE
                }
                catch (UnknownHostException e)
                {
                    e.printStackTrace();
                }
                record_result.setAlias(address_string);
                break;

            case CNAME:
                String cname_string = "";
                Resource cname_res = retrieveAliasInfo(counter);
                cname_string = cname_res.alias;
                record_result.setAlias(cname_string);
                break;

            case NS:
                String ns_string = "";
                Resource ns_res = retrieveAliasInfo(counter);
                ns_string = ns_res.alias;
                record_result.setAlias(ns_string);
                break;

            case MX:
                String mx_string = "";
                byte mx0 = this.response[counter];
                byte mx1 = this.response[counter+1];
                byte[] answer_mx = {mx0, mx1};
                // also need an int
                ByteBuffer wrapping2  = ByteBuffer.wrap(answer_mx);
                record_result.setMXPreference(wrapping2.getShort());
                mx_string = retrieveAliasInfo(counter+2).alias;
                record_result.setAlias(mx_string);
                break;

        }

        int finalLength = (answer_rdlength_int + counter - answer_index);
        record_result.setLengthOfByte(finalLength);
        return record_result;
    }

    private Resource retrieveAliasInfo(int offsetIndex)
    {
        Resource tempResource = new Resource();
        // populate the packet with the response data aliases
        String tempAlias = "";
        byte[] current_offset = {(byte)(response[offsetIndex] & 0x3F), response[offsetIndex + 1]}; //TODO unsure if this is right way of setting up
        // run through appropriate section
        boolean go = true;
        int counter = 0;

        while (response[offsetIndex] != 0)
        {
            if (go == false)
            {
                // number means replaced with period
                tempAlias += ".";
            }
            if ((int)0xC0 == (response[offsetIndex] & 0xC0))
            {
                ByteBuffer wrapping = ByteBuffer.wrap(current_offset);
                tempAlias += retrieveAliasInfo(wrapping.getShort()); // TODO GET ???
                counter += 2;
                offsetIndex +=2;
                response[offsetIndex] = 0;
            }
            else
            {
                // assemble the string from all the chars inputted
                String tempString = "";
                for(int i = 0; i < response[offsetIndex]; i++)
                {
                    tempString += (char)response[offsetIndex+1+i];
                }
                tempAlias += tempString;
                counter += (1 + response[offsetIndex]);
                offsetIndex += (1 + response[offsetIndex]);
                // TODO ensure its still itself? response[offsetIndex] = response[offsetIndex];
            }
            go = false;
        }
        tempResource.alias = tempAlias;
        tempResource.bs = counter;
        return tempResource;
    }

    public void response_output()
    {
        // check for no output
        if(this.ANCOUNT <= 0)
        {
            System.out.println("NOTFOUND");
            return;
        }
        // per assignment specifications
        System.out.println("***Answer Section (" + this.ANCOUNT + " answerRecords)***");
        // now output all the records from the received response
        for(Record record : answers)
        {
            record.record_out(); // print in the predetermined format based on type
        }
        System.out.println(); // give some space
        // now for the additionals
        if (this.ARCOUNT > 0) // there actually exist some
        {
            System.out.println("***Additional Section (" + this.ARCOUNT + " answerRecords)***");
            for(Record record : additionals)
            {
                record.record_out(); // print in the predetermined format based on type
            }
        }

    }
}

