package com.api.project.util;

import org.joda.time.DateTime;

public class LongUtil {

    public static String long2DateStr(Long input, String formate){

        if(input == null){

            return null;
        }
        try{
            if(StringUtil.isBlank(formate)){

                return new DateTime(input).toString();
            }else{

                return new DateTime(input).toString(formate);
            }
        }catch (Exception e){

            e.printStackTrace();
        }
        return null;
    }
}
