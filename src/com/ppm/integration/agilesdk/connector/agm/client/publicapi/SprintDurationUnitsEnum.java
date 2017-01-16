package com.ppm.integration.agilesdk.connector.agm.client.publicapi;




public enum SprintDurationUnitsEnum {


    DAYS("Days"),WEEKS("Weeks");

    private String text;

    SprintDurationUnitsEnum(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public static SprintDurationUnitsEnum fromText(String text){
        SprintDurationUnitsEnum result=null;
        if(text!=null){
            for (SprintDurationUnitsEnum item:SprintDurationUnitsEnum.values()){
                if(text.equalsIgnoreCase(item.getText())){
                    result= item;
                    break;
                }
            }
        }
        return result;
    }

}
