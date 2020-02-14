public class Record
{
    int time_to_live;
    int mx_type_preference;
    String alias;
    String name;
    QType q_type;
    boolean authorization_bool;
    String authorization_string;
    int byteLength;
    byte[] QClass;
    int rdlength;
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
    public QType getQType()
    {
        return q_type;
    }
    public void setLengthOfByte(int byteLength)
    {
        this.byteLength = byteLength;
    }

    public void setName(String name)
    {
        this.name = name;
    }
    public void setQType(QType q_type)
    {
        this.q_type = q_type;
    }
    public void setQClass(byte[] QClass)
    {
        this.QClass = QClass;
    }
    public void setTTL(int time_to_live)
    {
        this.time_to_live = time_to_live;
    }
    public void setRDLength(int rdlength)
    {
        this.rdlength = rdlength;
    }
    public void setAlias(String alias)
    {
        this.alias = alias;
    }
    public void setMXPreference(int mx_type_preference)
    {
        this.mx_type_preference = mx_type_preference;
    }




}
