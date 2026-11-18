package owner.model;

public class Bus {
    private int busId;
    private String registrationNumber;
    private int capacity;
    private String status;
    private double speed;

    public Bus(int busId,String registrationNumber,int capacity,String status,double speed){
        this.busId=busId;
        this.registrationNumber=registrationNumber;
        this.capacity=capacity;
        this.status=status;
        this.speed=speed;
    }
    public int getBusId(){
        return busId;
    }
    public String getRegistrationNumber(){
        return registrationNumber;
    }
    public int getCapacity(){
        return  capacity;
    }
    public double getSpeed(){
        return speed;
    }
    public String getStatus() {
        return status;
    }

    public void setStatus(String status){
        this.status=status;
    }
    public void setSpeed(double speed){
        this.speed=speed;
    }

}
