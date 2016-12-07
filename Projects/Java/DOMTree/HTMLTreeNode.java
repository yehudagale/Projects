import java.util.HashMap;
import java.util.ArrayList;
public class HTMLTreeNode
{
	private HashMap<String,String> atributes = new HashMap<String, String>();
	private ArrayList<HTMLTreeNode> children = new  ArrayList<HTMLTreeNode>();
	private HTMLTreeNode parent;
	private String tag;
	private String id = null;
	private boolean enteredId;
	HTMLTreeNode(HTMLTreeNode top, String nodeTag)
	{
		if(top != null){
			top.addChild(this);
		}
		this.tag = nodeTag;
		this.setParent(top);
	}
	// HTMLTreeNode(HTMLTreeNode top, String nodeTag, ArrayList<HTMLTreeNode> newChildren)
	// {
	// 	this(top, nodeTag);
	// 	this.addChildren(newChildren);
	// }
	public void addChild(HTMLTreeNode newChild)
	{
		children.add(newChild);
	}
	// public boolean removeChild(HTMLTreeNode child)
	// {
	// 	return children.remove(child);
	// }
	// public void addChildren(ArrayList<HTMLTreeNode> newChildren)
	// {
	// 	children.addAll(newChildren);
	// }
	public ArrayList<HTMLTreeNode> getChildren()
	{
		return new ArrayList<HTMLTreeNode>(children);
	}
	public HTMLTreeNode getParent()
	{
		return parent;
	}
	public void setParent(HTMLTreeNode top)
	{
		this.parent = top;
	}
	public String getAtribute(String key)
	{
		return atributes.get(key);
	}
	public HashMap<String, String> getAtributes()
	{
		return new HashMap<String, String>(atributes);
	}
	public String setAtribute(String key, String value)
	{
		if (key.equals("id")){
			String oldId = id;
			this.id = value;
			enteredId = true;
			return oldId;
		}
		return atributes.put(key, value);
	}
	public String setId(String newId){
		String oldId = id;
		this.id = newId;
		enteredId = false;
		return oldId;
	}
	public String getId()
	{
		return this.id;
	}
	public boolean enteredId()
	{
		return this.enteredId;
	}
	public String getTag()
	{
		return this.tag;
	}
}
