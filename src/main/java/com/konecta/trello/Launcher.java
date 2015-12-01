/**
 * 
 */
package com.konecta.trello;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author David
 *
 */
public class Launcher {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		@SuppressWarnings({ "unused", "resource" })
		ApplicationContext context = new ClassPathXmlApplicationContext(args[0]);

	}

}
