package com.example.modulethreeassignmentkalebward;

/*
 * Class: DataItem
 * Description: This class is for the actual inventory data items, it allows for various variables assigned
*/

public class DataItem {
    private long id = -1; 
    private String name;
    private String quantity;
    private String date;
    private String expirationDate; 

    public DataItem(String name, String quantity, String date) {
        this.name = name;
        this.quantity = quantity;
        this.date = date;
        this.expirationDate = "";
    }

    public DataItem(String name, String quantity, String date, String expirationDate) {
        this.name = name;
        this.quantity = quantity;
        this.date = date;
        this.expirationDate = expirationDate != null ? expirationDate : "";
    }

    /*
    * getId
    * Description: Receives ID of item
    */

    public long getId() {
        return id;
    }

    /*
    * setId
    * @params()
        *long id
    * Description: Sets the ID of the item
    */

    public void setId(long id) {
        this.id = id;
    }

    /*
    * getName
    * Description: Receives name of the item
    */

    public String getName() {
        return name;
    }

    /*
    * setName
    * @params()
        *string name
    * Description: Sets the name of the item
    */

    public void setName(String name) {
        this.name = name;
    }

    /*
    * getQuantity
    * Description: Receives quantity of the item
    */

    public String getQuantity() {
        return quantity;
    }

    /*
    * setQuantity
    * @params()
        *string quantity
    * Description: Sets the quantity of the item
    */

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    /*
    * getDate
    * Description: Receives posted date of the item
    */

    public String getDate() {
        return date;
    }

    /*
    * setDate
    * @params()
        *string date
    * Description: Sets the posted date of the item
    */

    public void setDate(String date) {
        this.date = date;
    }

    /*
    * getExpirationDate
    * Description: Receives experation date of the item
    */

    public String getExpirationDate() {
        return expirationDate;
    }

    /*
    * setExpirationDate
    * Description: sets experation date of the item
    */

    public void setExpirationDate(String expirationDate) {
        this.expirationDate = expirationDate != null ? expirationDate : "";
    }
}
