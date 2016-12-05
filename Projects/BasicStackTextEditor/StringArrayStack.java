import java.util.Arrays;
public class StringArrayStack
{
	private int stackerTracker;
	private String[] stack;
	StringArrayStack()
	{
		this.stack = new String[10];
		Arrays.fill(this.stack, 0, 10, null);
		this.stackerTracker = 0;
	}
	/**
	* Pushes a new String onto the stack. If the underlying array is full, expands the array.
	* @param toPush The string to push.
	*/
	public void push(String toPush)
	{
		if(stackerTracker >= stack.length){
			this.expand();
		}
		stack[stackerTracker] = toPush;
		stackerTracker++;
	}
	/**
	* Returns the top of the stack without removing it.
	* @return The top of the stack.
	*/
	public String peek()
	{
		if (stackerTracker < 1){
			return null;
		}
		return 	stack[stackerTracker - 1];
	}
	/**
	* pops the top item off the stack and returns it.
	* @return the top of the stack.
	*/
	public String pop()
	{
		stackerTracker--;
		if (stackerTracker < 0){
			stackerTracker = 0;
			return null;
		}
		return 	stack[stackerTracker];
	}
	/**
	*returns the size of the stack
	*@return the size of the stack.
	*/
	public int size()
	{
		return stackerTracker;
	}
	/**
	* expands the underlying array to accomadate more values.
	*/
	private void expand()
	{
		String[] tempStack = new String[2 * stack.length];
		for (int i = 0; i < stack.length; i++){
			tempStack[i] = stack[i];
		}
		Arrays.fill(tempStack, stack.length, tempStack.length, null);
		this.stack = tempStack;
	}
}
