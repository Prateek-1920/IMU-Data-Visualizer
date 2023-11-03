import javafx.application.Application;
import javafx.stage.Stage;
public class gui extends Application
{
  public static void main(String[] args) 
  {
     System.out.println("Launching JavaFX");
     launch(args);
     System.out.println("Finished");
  }
  public void start(Stage stage)
  {
     stage.setTitle("Hello");
     stage.setWidth(500);
     stage.setHeight(500);
     stage.show();
  }
}