package au.org.aekos.service.search.index;

import org.junit.Test;

public class PretendTest {

	@Test
	public void testMe(){
		int [] a = { 6,
				7,
				9,
				5,
				6,
				3,
				2
 };
			   
			   int maxDiff = 0; 
			   if(a.length < 2){
			       System.out.println("-1");
			   }
			   for(int x = 0; x < a.length - 1; x ++){
			       for(int y = x + 1; y < a.length ; y++){
			           if(x < y && a[x] < a[y]){
			               int diff = a[y] - a[x];
			               if(diff > maxDiff){
			                   maxDiff = diff;
			               }
			           }
			       }
			   } 
			   System.out.println(maxDiff);
			}
		
	}
	
	

