package com.api.project.util;

import com.api.project.vo.SysPlatformVO;

public class OsInfoUtil {

	private static String OS = System.getProperty("os.name").toLowerCase();  
    
    private static OsInfoUtil _instance = new OsInfoUtil();  
      
    private SysPlatformVO platform;  
      
    private OsInfoUtil(){}  
      
    public static boolean isLinux(){  
        return OS.indexOf("linux")>=0;  
    }  
      
    public static boolean isMacOS(){  
        return OS.indexOf("mac")>=0&&OS.indexOf("os")>0&&OS.indexOf("x")<0;  
    }  
      
    public static boolean isMacOSX(){  
        return OS.indexOf("mac")>=0&&OS.indexOf("os")>0&&OS.indexOf("x")>0;  
    }  
      
    public static boolean isWindows(){  
        return OS.indexOf("windows")>=0;  
    }  
      
    public static boolean isOS2(){  
        return OS.indexOf("os/2")>=0;  
    }  
      
    public static boolean isSolaris(){  
        return OS.indexOf("solaris")>=0;  
    }  
      
    public static boolean isSunOS(){  
        return OS.indexOf("sunos")>=0;  
    }  
      
    public static boolean isMPEiX(){  
        return OS.indexOf("mpe/ix")>=0;  
    }  
      
    public static boolean isHPUX(){  
        return OS.indexOf("hp-ux")>=0;  
    }  
      
    public static boolean isAix(){  
        return OS.indexOf("aix")>=0;  
    }  
      
    public static boolean isOS390(){  
        return OS.indexOf("os/390")>=0;  
    }  
      
    public static boolean isFreeBSD(){  
        return OS.indexOf("freebsd")>=0;  
    }  
      
    public static boolean isIrix(){  
        return OS.indexOf("irix")>=0;  
    }  
      
    public static boolean isDigitalUnix(){  
        return OS.indexOf("digital")>=0&&OS.indexOf("unix")>0;  
    }  
      
    public static boolean isNetWare(){  
        return OS.indexOf("netware")>=0;  
    }  
      
    public static boolean isOSF1(){  
        return OS.indexOf("osf1")>=0;  
    }  
      
    public static boolean isOpenVMS(){  
        return OS.indexOf("openvms")>=0;  
    }
    /** 
     * 获取操作系统名字 
     * @return 操作系统名 
     */  
    public static SysPlatformVO getOSname(){  
        if(isAix()){  
            _instance.platform = SysPlatformVO.AIX;  
        }else if (isDigitalUnix()) {  
            _instance.platform = SysPlatformVO.Digital_Unix;  
        }else if (isFreeBSD()) {  
            _instance.platform = SysPlatformVO.FreeBSD;  
        }else if (isHPUX()) {  
            _instance.platform = SysPlatformVO.HP_UX;  
        }else if (isIrix()) {  
            _instance.platform = SysPlatformVO.Irix;  
        }else if (isLinux()) {  
            _instance.platform = SysPlatformVO.Linux;  
        }else if (isMacOS()) {  
            _instance.platform = SysPlatformVO.Mac_OS;  
        }else if (isMacOSX()) {  
            _instance.platform = SysPlatformVO.Mac_OS_X;  
        }else if (isMPEiX()) {  
            _instance.platform = SysPlatformVO.MPEiX;  
        }else if (isNetWare()) {  
            _instance.platform = SysPlatformVO.NetWare_411;  
        }else if (isOpenVMS()) {  
            _instance.platform = SysPlatformVO.OpenVMS;  
        }else if (isOS2()) {  
            _instance.platform = SysPlatformVO.OS2;  
        }else if (isOS390()) {  
            _instance.platform = SysPlatformVO.OS390;  
        }else if (isOSF1()) {  
            _instance.platform = SysPlatformVO.OSF1;  
        }else if (isSolaris()) {  
            _instance.platform = SysPlatformVO.Solaris;  
        }else if (isSunOS()) {  
            _instance.platform = SysPlatformVO.SunOS;  
        }else if (isWindows()) {  
            _instance.platform = SysPlatformVO.Windows;  
        }else{  
            _instance.platform = SysPlatformVO.Others;  
        }  
        return _instance.platform;  
    }
    public static void main(String args[]){
    	System.out.println(OsInfoUtil.getOSname());  
    }
}
