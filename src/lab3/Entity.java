package lab3;

public interface Entity {

	public void sendText(String msg) throws Exception;

	public String readMessage() throws Exception;

	public void sendImage(String string) throws Exception;

}
