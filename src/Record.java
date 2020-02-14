public class Record
{
    int time_to_live;
    int mx_type_preference;
    String alias;
    QType q_type;
    boolean authorization_bool;
    String authorization_string;
    int byteLength;
    // rd length has a get set in here but is used in response
    // seems like name is never used
    // query class has a use of set in response
    // get set querytype are both used in response and query type is used once in here for sqitch
    // get and set of byt length used in response

    public Record(boolean authorization_bool)
    {
        // when creating a record, we must know the authorization, other characteristics dependent on types
        this.authorization_bool = authorization_bool;
    }

    // we have record objects, response will require these as outputs
    //response calls to get the records and based on their characteristics, records forms those outputs and gives them to resposne
    public void record_out()
    {
        // output of the record to response will depend on the type (a, cname, mx, ns)
        switch(this.q_type) // get the q_type of the record and output accordingly
        {
            case A: // IP type
                authorization_string = this.authorization_bool ? "auth" : " nonauth";
                System.out.println("IP" + "\t" + this.alias + "\t" + this.time_to_live + "\t" + this.authorization_string);
                break;

            case CNAME:
                authorization_string = this.authorization_bool ? "auth" : " nonauth";
                System.out.println("CNAME" + "\t" + this.alias + "\t" + this.time_to_live + "\t" + this.authorization_string);
                break;

            case MX:
                authorization_string = this.authorization_bool ? "auth" : " nonauth";
                System.out.println("MX" + "\t" + this.alias + "\t" + this.mx_type_preference + "\t" + this.time_to_live + "\t" + this.authorization_string);
                break;

            case NS:
                authorization_string = this.authorization_bool ? "auth" : " nonauth";
                System.out.println("NS" + "\t" + this.alias + "\t" + this.time_to_live + "\t" + this.authorization_string);
                break;

            default:
                // IS THIS WHERE WE PRINT "NOTFOUND???"
                break;
        }
    }
    public int getLengthOfByte()
    {
        return byteLength;
    }

    public void setLengthOfByte(int byteLength)
    {
        this.byteLength = byteLength;
    }


}