package edu.buffalo.cse.cse486586.groupmessenger2;



/**
 * Created by prasad-pc on 3/4/17.
 */

public class Message implements  Comparable<Message>{

    String message;
    String messageId;


    String status;
    String frompid;
    String topid;
    String seq;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getFrompid() {
        return frompid;
    }

    public void setFrompid(String frompid) {
        this.frompid = frompid;
    }

    public String getTopid() {
        return topid;
    }

    public void setTopid(String topid) {
        this.topid = topid;
    }

    public String getSeq() {
        return seq;
    }

    public void setSeq(String seq) {
        this.seq = seq;
    }
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString(){
        return "STATUS="+status+"Message="+message+ "MID="+messageId+"FROMPID="+frompid+"TOPID="+topid+"SEQ="+seq;
    }

    @Override
    public int compareTo(Message other) {
       // return this.getSeq().compareTo(other.getSeq());
        Integer a=Integer.parseInt(this.getSeq());
        Integer b=Integer.parseInt(other.getSeq());
        int ta=Integer.parseInt(this.getTopid());
        int tb=Integer.parseInt(other.getTopid());

        int r = Integer.compare(a, b);
        return r == 0 ? Integer.compare(ta, tb) : r;
    }

}
