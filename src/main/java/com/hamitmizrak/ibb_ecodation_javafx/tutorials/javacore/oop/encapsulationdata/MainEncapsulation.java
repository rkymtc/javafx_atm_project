package com.hamitmizrak.ibb_ecodation_javafx.tutorials.javacore.oop.encapsulationdata;

public class MainEncapsulation {

    public static void main(String[] args) {
        Person person=new Person();
        person.setName("Hamit");
        //person.setAge(-38);
        person.setAge(38);
        System.out.println("ADI: "+person.getName()+" YAS: "+person.getAge());
    }
}
