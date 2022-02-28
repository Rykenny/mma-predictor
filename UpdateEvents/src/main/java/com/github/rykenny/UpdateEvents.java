package com.github.rykenny;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.google.api.core.ApiFuture;
import com.google.api.core.ApiFutureCallback;
import com.google.api.core.ApiFutures;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.PubsubMessage;

//To Do
// Add functionality to write upcoming fights to a different pubsub (this will form the basis of prediciton) 


/**
 * UpdateEvents - scrapes UFC events page, identifies the most recent event, scrapes the most recent events page
 * input - triggered weekly
 * output - writes bout specific urls to pub/sub
 */
public class UpdateEvents implements BackgroundFunction<PubSubMessage>{
    public static void main(String[] args) throws IOException{
        HtmlPage eventsPage = webScraper("https://www.ufc.com/events#events-list-past");
        List<HtmlAnchor> eventLinks = eventsPage.getByXPath("//h3[@class='c-card-event--result__headline']/a");
        String mostRecentLink = "https://www.ufc.com".concat(eventLinks.get(eventLinks.size() - 8).getHrefAttribute());
        String topic = "most_recent_event";
        try {
            writeToPubSub(mostRecentLink, topic);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        
        
    }
/**
 * 
 * @param webPage
 * @return HtmlPage object for a given webpage
 * @throws FailingHttpStatusCodeException
 * @throws MalformedURLException
 * @throws IOException
 */
    public static HtmlPage webScraper(String webPage) {
        
        //initialize a headless browser
        WebClient webClient = new WebClient(BrowserVersion.CHROME);
   
        //configuring options    
        webClient.getOptions().setUseInsecureSSL(true);
        webClient.getOptions().setCssEnabled(false);        
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
        webClient.getOptions().setThrowExceptionOnScriptError(false);
        
        try {
        //fetching the web page        
        HtmlPage page = webClient.getPage(webPage);
        //close web client 
        webClient.close();
        return page;        
        }

        catch (FailingHttpStatusCodeException | IOException ex) {
            ex.printStackTrace();
            return null;
        }
        
       
   }

/**
 * method to write to pubsub topic
 * @param message - string with message to write to pubsub
 * @throws IOException
 * @throws InterruptedException
 */
   public static void writeToPubSub(String message,String topic) throws InterruptedException {
    Publisher publisher = null;
    try {
      publisher = Publisher.newBuilder(topic).build();
      ByteString data = ByteString.copyFromUtf8(message);
      PubsubMessage pubsubMessage = PubsubMessage.newBuilder().setData(data).build();
      ApiFuture<String> messageIdFuture = publisher.publish(pubsubMessage);
      ApiFutures.addCallback(messageIdFuture, new ApiFutureCallback<String>() {
        public void onSuccess(String messageId) {
          System.out.println("published with message id: " + messageId);
        }
    
        public void onFailure(Throwable t) {
          System.out.println("failed to publish: " + t);
        }
      }, MoreExecutors.directExecutor());
    } 
    catch (IOException ex){
        ex.printStackTrace();
    }
    finally {
      if (publisher != null) {
        publisher.shutdown();
        publisher.awaitTermination(1, TimeUnit.MINUTES);
      }
    }

   }
}