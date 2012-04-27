/**
 * 
 */
package uk.ac.ebi.xml.jaxb.utils;


import uk.ac.ebi.xml.jaxb.File;
import uk.ac.ebi.xml.jaxb.Folder;

/**
 * @author rpslpereira
 *
 */
public class ExperimentUtils {

	
	public static int getNumberFiles(Folder fold, String kind){
		int ret=0;
		if(fold!=null){
			for (File file : fold.getFile()) {
				if (file.getKind().equalsIgnoreCase(kind)){
					ret++;
				}
			}			
		}
		return ret;		
	}
	
	public static boolean hasFiles(Folder fold, String kind){
		if(fold!=null){
			for (File file : fold.getFile()) {
				if (file.getKind().equalsIgnoreCase(kind)){
					return true;
				}
			}			
		}
		return false;
		
	}
	
	
	public static File getFile(Folder fold, String kind){
		if(fold!=null){
			for (File file : fold.getFile()) {
				if (file.getKind().equalsIgnoreCase(kind)){
					return file;
				}
			}			
		}
		return null;
		
	}
	
	
}
