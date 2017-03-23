package marytts.language.cy;

/*
 * ****************************************************************************
 * Copyright (c) 2012 Prifysgol Bangor University
 * 
 * 	Redistribution and use of this software in source and binary forms, with or 
 * 	without modification, are permitted provided that the following conditions are met:
 * 
 * 		Redistributions of source code must retain the above copyright notice, this 
 * 		list of conditions and the following disclaimer.
 * 
 *		Redistributions in binary form must reproduce the above copyright notice, 
 *		this list of conditions and the following disclaimer in the documentation 
 *		and/or other materials provided with the distribution.
 *
 *	The name of Prifysgol Bangor University may be used to endorse or promote products 
 *	derived from this software without specific prior written permission of Prifysgol
 *	Bangor University.
 *
 *	THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND 
 *	ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED 
 *	WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. 
 *	IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, 
 *	INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, 
 *	BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, 
 *	OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, 
 *	WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
 *	ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY 
 *	OF SUCH DAMAGE.
 * 
 * ******************************************************************************
 */
import java.util.ArrayList;
import java.util.List;

public class Welsh_Base_10 {
		
	private static class Number_1 {		
		public static String lemma() { return "un";}
	};
	
	private static class Number_2 {
		public static String lemma() { return "dau"; }
		public static String lemma_fem() { return "dwy"; }		
	}
	
	private static class Number_3 {
		public static String lemma() { return "tri"; }
		public static String lemma_fem() { return "tair"; }		
		public static String aspirate(){ return "thri"; }
		public static String aspirate_fem(){ return "thair"; }		
	}
	
	private static class Number_4 {
		public static String lemma() { return "pedwar"; }
		public static String lemma_fem() { return "pedair"; }
		public static String aspirate(){ return "phedwar"; }
		public static String aspirate_fem(){ return "phedair"; }		
	}
	
	private static class Number_5 {
		public static String lemma() { return "pump"; }
		public static String lemma_fem() { return "pum"; }
		public static String aspirate(){ return "phump"; }
		public static String aspirate_fem(){ return "phum"; }		
	}
	
	private static class Number_6 {
		public static String lemma() { return "chwech"; }
		public static String lemma_fem() { return "chwe"; }
	}
	
	private static class Number_7 {
		public static String lemma() { return "saith"; }
	}
	
	private static class Number_8 {
		public static String lemma() { return "wyth"; }
	}
	
	private static class Number_9 {
		public static String lemma() { return "naw"; }
	}
	
	private static class Number_10 {
		public static String lemma() { return "deg"; }		
		public static String soft(){ return "ddeg"; }			
	}
	
	private static class Number_100 {
		public static String lemma() { return "cant"; }
		public static String lemma_fem() { return "can"; }
		public static String soft(){ return "gant"; }
		public static String soft_lemma_fem(){ return "gan"; }
		public static String aspirate(){ return "chant"; }
		public static String aspirate_fem(){ return "chan"; }
	}
	
	private static class Number_1000 {
		public static String lemma() { return "mil"; }		
		public static String soft(){ return "fil"; }			
	}
	
	private static class Number_1000000 {
		public static String lemma() { return "miliwn"; }		
		public static String soft(){ return "filiwn"; }			
	}
		
	private static class Word_a {
		public static String lemma() { return "a";}
		public static String c() { return "ac";}
		public static String g() { return "ag";}
	}
	
		
	public static String tokenise(double number, 
								  boolean isFeminine){
		// angen cynorthwyo '... pwynt ...' hefyd 		
		Double numberDouble=number;		
		int numberInt = numberDouble.intValue();
		String numberString = Integer.toString(numberInt);

		List<String> result = new ArrayList<String>(numberString.length());	
		tokenise(result, numberString, isFeminine);
		
		StringBuilder sb = new StringBuilder();
		for (int i=0;i<result.size();i++){
			sb.append(result.get(i));
			sb.append(" ");
		}
		
		return sb.toString();
		
	}	
	
	public static void tokenise(List<String> tokens, 
								String numberString, 
								boolean isFeminine){
		
		try {
			Integer value = Integer.parseInt(numberString);
			number_from_integer(tokens,value,isFeminine);
		} catch (NumberFormatException exc){
			System.out.println("Number Format Exception " + exc.getMessage());
		}
						
	}	

	private static void number_from_integer(List<String> tokens,
											int n, 
											boolean isFeminine)
	{
		
		int original_tokens_size = tokens.size();
		
		switch (n){
			case 1: tokens.add(Number_1.lemma()); break;			
			case 2: tokens.add(isFeminine ? Number_2.lemma_fem() : Number_2.lemma()); break;		
			case 3: tokens.add(isFeminine ? Number_3.lemma_fem() : Number_3.lemma()); break;	
			case 4: tokens.add(isFeminine ? Number_4.lemma_fem() : Number_4.lemma()); break; 
			case 5: tokens.add(isFeminine ? Number_5.lemma_fem() : Number_5.lemma()); break;
			case 6: tokens.add(isFeminine ? Number_6.lemma_fem() : Number_6.lemma()); break;
			case 7: tokens.add(Number_7.lemma()); break;
			case 8: tokens.add(Number_8.lemma()); break;
			case 9: tokens.add(Number_9.lemma()); break;
			case 10: tokens.add(Number_10.lemma()); break;										
		}
				
		if (tokens.size() == original_tokens_size)
		{					
			if ((n <= 19) && (n>= 11)){
				tokens.add(Number_1.lemma());  
				tokens.add(Number_10.lemma()); 
				number_from_integer(tokens, n - 10, false);
			}
			else if ((n <= 29) && (n>= 20)) {
				tokens.add(Number_2.lemma());
				tokens.add(Number_10.soft());
				number_from_integer(tokens, n - 20, false);
			}
			else if ((n <= 39) && (n>= 30)) {
				tokens.add(Number_3.lemma());
				tokens.add(Number_10.lemma()); 
				number_from_integer(tokens, n - 30, false);
			}
			else if ((n <= 49) && (n>= 40)) {
				tokens.add(Number_4.lemma()); 
				tokens.add(Number_10.lemma()); 
				number_from_integer(tokens, n - 40, false);
			}
			else if ((n <= 59) && (n>= 50)) {
				tokens.add(Number_5.lemma_fem());
				tokens.add(Number_10.lemma());
				number_from_integer(tokens, n - 50, false);
			}
			else if ((n <= 69) && (n>= 60)) {
				tokens.add(Number_6.lemma_fem());
				tokens.add(Number_10.lemma());
				number_from_integer(tokens, n - 60, false);
			}
			else if ((n <= 79) && (n>= 70)) {
				tokens.add(Number_7.lemma());
				tokens.add(Number_10.lemma());
				number_from_integer(tokens, n - 70, false);
			}
			else if ((n <= 89) && (n>= 80)) {
				tokens.add(Number_8.lemma());
				tokens.add(Number_10.lemma());
				number_from_integer(tokens, n - 80, false);
			}
			else if ((n <= 99) && (n>= 90)) {
				tokens.add(Number_9.lemma());
				tokens.add(Number_10.lemma());
				number_from_integer(tokens, n - 90, false);
			}
			else if ((n <= 999) && (n>= 100)) {

				int u = n % 100;
				switch (u){
					case 3:
					case 4:
					case 5:					
						hundreds_from_integer(tokens, n);
						tokens.add(Word_a.lemma());						
						switch (u){
							case 3: tokens.add(Number_3.aspirate()); break;
							case 4: tokens.add(Number_4.aspirate()); break;
							case 5: tokens.add(Number_5.aspirate()); break;							
						}
						break;
				
					default:
						hundreds_from_integer(tokens, n);		
						if ((u <= 10) && (u>=1)){
							switch (u){
								case 1: tokens.add(Word_a.c()); break;
								case 8: tokens.add(Word_a.c()); break;
								default : tokens.add(Word_a.lemma()); break;
							}
						}
						number_from_integer(tokens, u, false);
						break;
				}
			}
			else if ((n<=999999) && (n >= 1000)) {
				int u = n % 1000;
				
				thousands_from_integer(tokens, n);
				
				switch (u) {
					case 1:						
						tokens.add(Word_a.c());
						tokens.add(Number_1.lemma());
						break;
					case 3:
					case 4:
					case 5:
						tokens.add(Word_a.lemma());
						switch (u){
							case 3: tokens.add(Number_3.aspirate()); break;
							case 4: tokens.add(Number_4.aspirate()); break;
							case 5: tokens.add(Number_5.aspirate()); break;
							case 100: tokens.add(Number_100.aspirate()); break;
						}
						break;
					case 8: 
						tokens.add(Word_a.c());
						tokens.add(Number_8.lemma());
						break;
					case 100:
						tokens.add(Word_a.c());
						tokens.add(Number_1.lemma());
						tokens.add(Number_100.lemma()); 
						break;
						
					default:
						if (u >=1 && u <=10){
							tokens.add(Word_a.lemma());
						}
						number_from_integer(tokens, u, false);
				}
			}
			else if ((n>=1000000) && (n <=999999999)) {
				
				int u = n % 1000000;
				
				millions_from_integer(tokens,n);
				
				switch (u) {
				
					case 1:						
						tokens.add(Word_a.c());
						tokens.add(Number_1.lemma());
						break;
							
					case 3:
					case 4:
					case 5:
						tokens.add(Word_a.lemma());
						switch (u){
							case 3: tokens.add(Number_3.aspirate()); break;
							case 4: tokens.add(Number_4.aspirate()); break;
							case 5: tokens.add(Number_5.aspirate()); break;
							case 100: tokens.add(Number_100.aspirate()); break;
						}
						break;
						
					case 8: 
						tokens.add(Word_a.c());
						tokens.add(Number_8.lemma());
						break;
						
					case 100:
						tokens.add(Word_a.c());
						tokens.add(Number_1.lemma());
						tokens.add(Number_100.lemma()); 
						break;
						
					default:
						if (u >=1 && u <=10){
							tokens.add(Word_a.lemma());
						}
						number_from_integer(tokens, u, false);
						break; 
				}			
			} 
			
		}	
		
	}
	
	
	private static void millions_from_integer(List<String> tokens, int n) {
		
		int m = n / 1000000;
		int hm = m / 100;
		int tm = m % 100;	
		
		if (m <= 6) {
			low_millions_from_integer(tokens, m);			
		} else if (tm == 0) {
			hundred_thousands_or_millions_from_integer(tokens,hm);
			tokens.add(Number_1000000.lemma());
		} else if ((tm >= 1) && (tm <= 6)) {
			low_millions_with_hundreds(tokens,m);
		} else {
			number_from_integer(tokens, m, false);
			tokens.add(Number_1000000.lemma());
		}
		
	}

	
	private static void low_millions_with_hundreds(List<String> tokens, int n) {
					
		hundreds_from_integer(tokens, n);
		
		int m = n %  100;
		
		switch (m){
			case 1:  
				tokens.add(Word_a.g());
				tokens.add(Number_1.lemma());
				tokens.add(Number_1000000.soft());
				break;
			default:
				tokens.add(Word_a.lemma());
				switch (m){
					case 2: tokens.add(Number_2.lemma_fem()); break;
					case 3: tokens.add(Number_3.aspirate_fem()); break;
					case 4: tokens.add(Number_4.aspirate_fem()); break;
					case 5: tokens.add(Number_5.aspirate_fem()); break;
					case 6: tokens.add(Number_6.lemma_fem()); break;
				}
				tokens.add(Number_1000000.lemma());
				break;
		}
		
	}

	private static void low_millions_from_integer(List<String> tokens, int n) {
		
		switch (n) {
		
			case 1 :
			case 2 :
				switch (n){
					case 1: tokens.add(Number_1.lemma()); break;
					case 2: tokens.add(Number_2.lemma_fem()); break;
				}
				tokens.add(Number_1000000.soft());
				break;
			default:
				switch (n){
					case 3: tokens.add(Number_3.lemma_fem()); break;
					case 4: tokens.add(Number_4.lemma_fem()); break;
					case 5: tokens.add(Number_5.lemma_fem()); break;
					case 6: tokens.add(Number_6.lemma_fem()); break;
				}
				tokens.add(Number_1000000.lemma());
				break;
				
		}
		
	}

	private static void hundreds_from_integer(List<String> tokens, 
											  int n) {
		
		int h = n / 100; // hundreds part
		
		switch (h) {
				
			case 1:
				tokens.add(Number_1.lemma());
				tokens.add(Number_100.lemma());
				break;
	
			case 2:
				tokens.add(Number_2.lemma());
				tokens.add(Number_100.soft());
				break;
 
			case 3:
				tokens.add(Number_3.lemma());
				tokens.add(Number_100.aspirate());
				break;
					
			case 5:
				tokens.add(Number_5.lemma_fem());
				tokens.add(Number_100.lemma());
				break;
						
			case 6:
				tokens.add(Number_6.lemma_fem());
				tokens.add(Number_100.aspirate());
				break;
					
			default:
				
				switch (h){
					case 4: tokens.add(Number_4.lemma());break;
					case 7: tokens.add(Number_7.lemma());break;
					case 8: tokens.add(Number_8.lemma());break;
					case 9: tokens.add(Number_9.lemma());break;
				}
				
				tokens.add(Number_100.lemma());						
				break;			
		}
	}

	
	private static void thousands_from_integer(List<String> tokens, 
											   int n){
		
		int t = n / 1000; // thousands part
		int ht = t / 100; // hundreds of thousands part
		int tm = t % 100;
		
		if (t <= 6){
			low_thousands_from_integer(tokens,t);
		} else if (tm==0){
			hundred_thousands_or_millions_from_integer(tokens, ht);
			tokens.add(Number_1000.lemma());
		} else if ((tm>=1) && (tm <=6)){
			low_thousands_with_hundreds(tokens, t);		
		} else {
			number_from_integer(tokens, t, true);
			tokens.add(Number_1000.lemma());
		}
				
	}


	private static void low_thousands_with_hundreds(List<String> tokens, int n) {
		
		hundreds_from_integer(tokens, n);
		
		int t = n % 100;
		
		switch (t){
		
			case 1:
				tokens.add(Word_a.c());
				tokens.add(Number_1.lemma());
				tokens.add(Number_1000.soft());
				break;
							
			case 8:
				tokens.add(Word_a.c());
				tokens.add(Number_1.lemma());
				tokens.add(Number_1000.lemma());
				break;
								
			default:
				tokens.add(Word_a.lemma());
				number_from_integer(tokens,t,false);
				tokens.add(Number_1000.lemma());
				break;
					
		}
		
	}


	private static void hundred_thousands_or_millions_from_integer(List<String> tokens, 
																   int n) {
		switch (n) {
			case 1: tokens.add(Number_100.lemma_fem());	break;
			case 2:
				tokens.add(Number_2.lemma());
				tokens.add(Number_100.soft_lemma_fem());
				break;
			case 3:
				tokens.add(Number_3.lemma());
				tokens.add(Number_100.aspirate_fem()); 
				break;
			case 4:
				tokens.add(Number_4.lemma());
				tokens.add(Number_100.lemma_fem());
				break;
			case 5:
				tokens.add(Number_5.lemma_fem());
				tokens.add(Number_100.lemma_fem()); 
				break;
			case 6:
				tokens.add(Number_6.lemma_fem());
				tokens.add(Number_100.aspirate_fem()); 
				break;
			case 7: 
			case 8:
			case 9:
				
				switch (n){
					case 7: tokens.add(Number_7.lemma()); break;
					case 8: tokens.add(Number_8.lemma()); break;
					case 9: tokens.add(Number_9.lemma()); break;
				}				
				tokens.add(Number_100.lemma_fem());				
				break;
			
		}	
				
	}


	private static void low_thousands_from_integer(List<String> tokens, int n) {
		
		switch (n){
		
			case 2: tokens.add(Number_2.lemma_fem()); break;
			case 3: tokens.add(Number_3.lemma_fem()); break;
			case 4: tokens.add(Number_4.lemma_fem()); break;
			case 5: tokens.add(Number_5.lemma_fem()); break;
			case 6: tokens.add(Number_6.lemma_fem()); break;
			default : number_from_integer(tokens, n, true); break;
			
		}
		
		switch (n){

			case 1:
			case 2:			
				tokens.add(Number_1000.soft());
				break;
			
			default:
				tokens.add(Number_1000.lemma());
		}
		
	}
	
	
}
