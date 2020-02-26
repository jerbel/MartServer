package org.occiware.mart.server.model.adapter;



import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EContentAdapter;

public class EntityAdapter extends EContentAdapter {
	
	private String qName;
	
	private EntityAdapter(String qName) {
		this.qName = qName;
	}
	
    	@Override
    	public void notifyChanged(Notification msg) {
    		super.notifyChanged(msg);	
    		System.out.println(msg.getEventType());
    		switch(msg.getEventType()) {
    			case Notification.RESOLVE:
    				System.out.println("Resolve notification!");
    				break;
    			case Notification.ADD:
    				EObject obj = (EObject) msg.getNewValue();
    				addAdapter(obj);
    				System.out.println("Add notification!");
    				System.out.println(msg.getNewValue());
    				send(msg.toString());
    				break;
    			case Notification.CREATE:
    				System.out.println("Create notification DEPRICATED!");
    				send(msg.toString());
    				break;
    			case Notification.ADD_MANY:
    				System.out.println("add many!");
    				send(msg.toString());
    				break;
    			case Notification.MOVE:
    				System.out.println("move!");
    				send(msg.toString());
    				break;
    			case Notification.NO_FEATURE_ID:
    				System.out.println("-1 feature id/index missing!");
    				send(msg.toString());
    				break;
    			case Notification.REMOVE:
    				System.out.println("Remove notification!");
    				send(msg.toString());
    				//Entity ent = (Entity) msg.getNotifier();
    				//System.out.println("Remove: " + ent.getLocation());
    				break;
    			case Notification.REMOVE_MANY:
    				System.out.println("remove many!");
    				send(msg.toString());
    				break;
    			case Notification.REMOVING_ADAPTER:
    				System.out.println("remove adapter!");
    				send(msg.toString());
    				break;
    			case Notification.SET:
    				System.out.println("set!");
    				send(msg.toString());
    			 	break;
    			case Notification.UNSET:
    				System.out.println("unset!");
    				send(msg.toString());
    				break;
    		}
    	}
    	
    	private void send(String message){
    		ConnectionFactory factory = new ConnectionFactory();
            factory.setHost("HOST");
            factory.setUsername("USER");
            factory.setPassword("PASS");
            factory.setPort(5672);
            try (Connection connection = factory.newConnection();
                Channel channel = connection.createChannel()) {
                channel.queueDeclare(qName, false, false, false, null);
                channel.basicPublish("", qName, null, message.getBytes(StandardCharsets.UTF_8));
                System.out.println(" [x] Sent '" + message + "'");
            } catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (TimeoutException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
    	}

    		
    		
    	
    	public static void addAdapter(EObject obj, String owner) {
    		for(Adapter adapter: obj.eAdapters()) {
    			if(adapter instanceof EntityAdapter) {
    				return;
    			}
    		}
    		obj.eAdapters().add(new EntityAdapter(owner));
    	}
   }
