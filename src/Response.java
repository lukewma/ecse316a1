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







        return null;
    }

    private Resource retrieveAliasInfo(int offsetIndex)
    {
        Resource tempResource = new Resource();
        // populate the packet with the response data aliases
        String tempAlias = "";
        byte[] current_offset = {(byte)(response[offsetIndex] & 0x3F), response[offsetIndex + 1]};
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
}