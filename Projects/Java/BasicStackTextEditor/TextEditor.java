/**used this code:
// /**
//  * InputReader reads typed text input from the standard text terminal. 
//  * The text typed by a user is returned.
//  * 
//  * @author     Michael kolling and David J. Barnes
//  * name changed for text editor comfort
//  * @version    0.1 (2011.07.31)

// public class InputReader
// {
//     private Scanner reader;

//     /**
//      * Create a new InputReader that reads text from the text terminal.
//      */
//     public InputReader()
//     {
//         reader = new Scanner(System.in);
//     }

//     /**
//      * Read a line of text from standard input (the text terminal),
//      * and return it as a String.
//      *
//      * @return  A String typed by the user.
//      */
//     public String getInput()
//     {
//         System.out.print("> ");         // print prompt
//         String inputLine = reader.nextLine();

//         return inputLine;
//     }
// }
//Also used this article http://cr.openjdk.java.net/~briangoetz/lambda/lambda-translation.html among others to learn about
// lambda expressions and function objects.
import java.util.Scanner;
import java.util.function.Consumer;
import java.lang.NumberFormatException;
public class TextEditor
{
	private StringArrayStack textStack;
	private Scanner reader;
	public static void main(String[] args)
	{
		TextEditor thing = new TextEditor();
		thing.run();
	}
	TextEditor()
	{
		textStack = new StringArrayStack();
		reader = new Scanner(System.in);
	}
	/**
	* runs the TextEditor.
	*/
	public void run()
	{
		String input;
		boolean done = false;
		while(!done){
			input = this.getInput();
			if(input.startsWith("#")){
				done = this.analyzeCommand(input); 
			}
			else{
				this.addLine(input);
			}
		}
	}
	 /**
     * Read a line of text from standard input (the text terminal),
     * and return it as a String.
     *
     * @return  A String typed by the user.
     */
	private String getInput()
    {
        System.out.print("> ");         // print prompt
        String inputLine = this.reader.nextLine();
        return inputLine;
    }
    /**
    * checks which command has been inputed and calls the appropriate method. Returns a boolean indicating whether
    * or not the TextEditor should stop running.
    *
    * @param input the command to be analyzed
    * @return Is the TextEditor done running?
    */
	private boolean analyzeCommand(String input)
	{
		if(input.equals("#exit")){
			return true;
		}
		else if (input.equals("#print")) {
			this.printAll();
		}
		else if (input.startsWith("#print ")) {
			this.printLine(input);
		}
		else if (input.equals("#delete")) {
			this.reset();
		}
		else if (input.startsWith("#delete ")) {
			this.deleteLine(input);
		}
		else{
			this.addLine(input);
		}
		return false;
	}
	/**  adds line to the text stack.
	* @param line the line to be added.
	*/
	private void addLine(String line)
	{
		this.textStack.push(line);
	}
	/**
	* empties the text stack.
	*/
	private void reset()
	{
		//it is easier to reinitialize the stack then to empty it.
		textStack = new StringArrayStack();
	}
	/**
	* prints all stored lines.
	*/
	private void printAll()
	{
		if (this.textStack.size() == 0) {
			System.out.println();
			return;
		}
		StringArrayStack tempStack = new StringArrayStack();
		//put all items into a reversed tempStack
		pushOnto(textStack, tempStack);
		//put them back into the original stack printing as we go.
		pushOnto(tempStack, textStack, p -> {System.out.println(p);});
	}
	/**
	*deletes one line.
	*@param input the delete command inputed.
	*/ 
	private void deleteLine(String input)
	{
		try{
			this.bringToTop(this.textStack, Integer.parseInt(input.substring(8)), p -> {p.pop();});
		}
		catch(NumberFormatException e){
			System.out.println("please choose an integer line number");
		}
	}
	/**
	*prints one line.
	*@param input the print command inputed.
	*/ 
	private void printLine(String input)
	{
		try{
			this.bringToTop(this.textStack, Integer.parseInt(input.substring(7)),	
					p -> {System.out.println(p.peek());});
			//can obviously be done without peek by making a new method which pops and then pushes
			// instead of lambda function use TextEditor::peek and uncomment the peek method later on.
		}
		catch(NumberFormatException e){
			System.out.println("please choose an integer line number");
		}
	}
	/**
	* Brings a given item on lineNumber to the top of stack then does action to the stack. It then returns the
	* removed items to the stack.   
	*
	*@param stack the stack to be manipulated
	*@param lineNumber the line number to be acted upon
	*@param action the action to be performed
	*/
	private static void bringToTop(StringArrayStack stack, int lineNumber, Consumer<StringArrayStack> action)
	{
		StringArrayStack tempStack = new StringArrayStack();
		lineNumber = stack.size() - lineNumber;
		if (lineNumber < 0) {
			System.out.println("line index out of range");
			return;
		}
		for(int i = 0; i < lineNumber; i++){
			tempStack.push(stack.pop());
		}
		action.accept(stack);
		pushOnto(tempStack, stack);
	}
	// /**
	// * Alternate peek method.
	// * @return the top of the stack.
	// */
	// private static peek(StringArrayStack stack)
	// {
	// 	String temp = stack.pop();
	// 	stack.push(temp);
	// 	return temp;
	// }
	/**
	* Takes sourceStack and pushes it onto destStack until sourceStack is empty. Performs an action 
	* on each string as it is pushing.
	*
	* @param sourceStack the stack to be emptied
	* @param destStack the stack to empty it into
	* @param action the action to perform on each String
	*
	*/
	private static void pushOnto(StringArrayStack sourceStack, StringArrayStack destStack, Consumer<String> action)
	{
		String temp = sourceStack.pop();
		while(temp != null)
		{
			action.accept(temp);
			destStack.push(temp);
			temp = sourceStack.pop();
		}
	}
	/**
	* Takes sourceStack and pushes it onto destStack until sourceStack is empty 
	*
	* @param sourceStack the stack to be emptied
	* @param destStack the stack to empty it into
	*/
	private static void pushOnto(StringArrayStack sourceStack, StringArrayStack destStack)
	{
		pushOnto(sourceStack, destStack, p -> {});
	}

}
