import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
/*
EXAMPLE REDUCTION:
(x||y)&&(x||z)&&(!x||q)&&(!x||z)&&(y||!q)&&(!x||!q)
!x&&y&&z&&(q||!q)
1: y,z
2: x
3: 
4: q
STEP BY STEP:
1. (x||y)&&(x||z)&&(!x||q)&&(!x||z)&&(y||!q)&&(!x||!q)
2. ((x)||(y&&z))&&(!x||q)&&(!x||z)&&(y||!q)&&(!x||!q)
3. ((x&&q)||(y&&z&&!x))&&(!x||z)&&(y||!q)&&(!x||!q)
	 ((x&&q&&z)||(y&&z&&!x))&&(y||!q)&&(!x||!q)
4. ((x&&q)||(y&&!x))&&z&&(y||!q)&&(!x||!q)
	 ((x&&q&&y)||(y&&!x))&&z&&(!x||!q)
5. ((x&&q)||(!x))&&y&&z&&(!x||!q)
6. (!x)&&y&&z&&(q||!q)


RULES:
for this purpose a clause is 2 sets of variables, within each set the variables are connected by "and"s.
the two sets are connected to each other by an "or".
the goal is too take all the variables in a 2CNF and sort them into 4 groups:
1. Must be true
2. Must be false
3. member of a clause (where no 2 clauses share any variables and no variable in a clause is in any other group).
4. has no constraints.
These four groups completely describe the 2CNF.
if no variable belongs to both 1 and 2, the 2CNF is satisfiable.
If a variable belongs to both 1 and 2, we abort.

To reduce the CNF this way, we take two clauses that share at least one variable, and insert one into the other.
to make things easier, we will make sure to only insert regular 2CNF clauses into a bigger clause
Since they share a literal, at least one side of the bigger clause will always either automatically satisfy the two clause or
require a particular answer to satisfy the two clause. The other side of the bigger clause takes the remaining answer.
If the remaining answer is unfit for the other side of the big clause (it is incompatible with the existing items),
it takes the same answer as the first side.
if both answers are unfit, the side disappears and all the variables on the first side drop into must haves.
Similarly, if the first side cannot take the answer remaining then the first side is eliminated
and the if the second side satisfies the equation (see example step 5) then we eliminate the first side and the inserted clause
otherwise we eliminate the first side and keep the inserted clause 
(ex. inserting (!q||!e) into (q&&e&&w||z&&b) yields (!q||!e)&&z&&b) with w joining group 4)
once their are no more clauses to insert we have reduced it
*/
public class CNF2 {
	private HashMap<String, HashSet<Clause>> trueMap;
	private HashMap<String, HashSet<Clause>> falseMap;
	private HashSet<String> mustBeTrue;
	private HashSet<String> mustBeFalse;
	private HashSet<String> noConstraints;
	private HashSet<Clause> clauses;
	private boolean satisfiable = false;
	private MyLinkedHashSet<Clause> clausesToParse;
	public static void main(String[] args) {
		CNF2 thing = new CNF2();
		thing.read(new File("./input"));
		thing.mapTest();
		thing.reduce();
		System.out.println(thing.count());
	}
	public void read(File file){
		try{
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String line = reader.readLine();
			int comma;
			Clause newClause;
			trueMap = new HashMap<String, HashSet<Clause>>();
			falseMap = new HashMap<String, HashSet<Clause>>();
			while(line != null){
				comma = line.indexOf(",");
				newClause = new Clause();
				addVarToSet(0, line.substring(0, comma), newClause);
				addVarToSet(2, line.substring(comma + 1), newClause);
				if (line.substring(0, comma).equals("!" + line.substring(comma + 1)) 
							|| ("!" + line.substring(0, comma)).equals(line.substring(comma + 1))) {
					newClause.bothTrue = false;
				}
				line = reader.readLine();
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	public void mapTest()
	{
		System.out.println("TRUE");
		for (String item: trueMap.keySet()) {
			for (Clause var : trueMap.get(item)) {
				for (String variable : var.getAllVars()) {
					System.out.println(item + " : " + variable);
				}
			}
		}
		System.out.println("FALSE");
		for (String item: falseMap.keySet()) {
			for (Clause var : falseMap.get(item)) {
				for (String variable : var.getAllVars()) {
					System.out.println(item + " : " + variable);
				}
			}
		}
	}
	public void reduce()
	{
		clausesToParse = new MyLinkedHashSet<Clause>();
		mustBeTrue = new HashSet<String>();
		mustBeFalse = new HashSet<String>();
		noConstraints = new HashSet<String>();
		clauses = new HashSet<Clause>();
		HashSet<String> set = new HashSet<String>(trueMap.keySet());
		set.addAll(falseMap.keySet());
		HashSet<Clause> items;
		Clause mainClause;
		satisfiable = true;
		for (String variable : set) {
			clausesToParse.addAll(trueMap.get(variable));
			clausesToParse.addAll(falseMap.get(variable));
			mainClause = clausesToParse.poll();
			if (mainClause != null) {
				clauses.add(mainClause);
				while(!clausesToParse.isEmpty()){
					if(!insert(mainClause, clausesToParse.remove())){
						clausesToParse.clear();
						satisfiable = false;
					}
				}
			}
			if (!satisfiable) {
				break;
			}
		}
	}
	/*
	cases
	(
		checkSuitability:
		   1. is compatible with one
		   2. is compatible with two
		   3. automatically satisfies one
		   4. automatically satisfies two
	)
	0. one or more variables is in must have or must not have.
		DO: Should never happen (remove those clauses immediately upon adding to musts)
	Call set 1 the one with the relationship to a variable
	1. set 1 automatically satisfies one of them
		a. set 2 automatically satisfies the other one.
			DO: nothing
		b. set 2 is compatible with the other one
			DO:
				add other one to set 2
				check for musts in the big clause
				check for bothTrue becoming false
		c. set 2 is compatible with the same one 
			DO:
				add same one to must haves (check for impossibilities)
				remove same one from set 1
				check other one for no Constraints
		d. (conclude)set 2 is mutually exclusive with both
			DO:
				remove side procedure on set 2
	2. set 1 is mutually exclusive with one of them.
		i. set 2 automatically satisfies the same one.
			a. set 1 automatically satisfies the other one:
					DO:nothing
			b. set 1 is compatible with the other one
				DO:
					add the compatible one to set one
					check for musts in the big clause
					(Both true check)
			c. (conclude)set 1 is mutually exclusive with both
				DO:
					remove side procedure on set 1
		ii. set 2 is compatible with the same one.
			a. set 1 automatically satisfies the other one
				DO:
					add the first one to set 2
					(both true is false)
			b. set 1 is compatible with the other one
				DO:
					add the other one to set 1
					add the first one to set 2
					check for musts
					set bothTrue to false
			c. (conclude)set 1 is mutually exclusive with both
				i.set 2 is incompatible with the first one 
					DO:
						add the other one to set 2
						remove side procedure on set 1
				ii.set 2 is compatible with the first one
					DO:
						special remove side (make the old insert clause the main one)
	
	if 1 or 2.
		DO:remove clause from maps
	3. Neither one is related
		DO:
			leave clause in maps
	*/
	private boolean insert(Clause bigClause, Clause toInsert)
	{
		//since I don't know which variables go in which clause, I pick at random...
		int side1 = 0;
		int side2 = 2;
		String var1 = toInsert[whichSet[0]].iterator().next();
		String var2 = toInsert[2 + whichSet[1]].iterator().next();
		boolean[] varValues;
		//then I check if I picked right
		Set<String> set = bigClause.getAllVars(); 
		if (!set.contains(var1)) {
			if (set.contains(var2)) {
				String temp = var1;
				var1 = var2;
				var2 = temp;
				toInsert.reverse();
			}
			else{
				return true;
			}
		}
		if(!bigClause.sets[side1].contains(var1) && !bigClause.sets[side1 + 1].contains(var1))
		{
			side1 = 2;
			side2 = 0;
		}
		varValues = whichSetConverter(toInsert);
		if(automaticallySatisfies(bigClause, var1, varValues[0], side1))
		{
			if (automaticallySatisfies(bigClause, var2, varValues[1], side2)) {
				
			}
			else if (isCompatible(bigClause, var2, varValues[1], side2)) {
				bigClause.sets[toInsert.whichSet[1] + side2]
			}
		}
	}
	private boolean[] whichSetConverter(Clause clause)
	{
		ret = new boolean[2];
		for (int i = 0; i <= 2; i++) {
			ret[i] = clause.whichSet[i] == 0;
		}
		return ret;
	}
	private boolean automaticallySatisfies(Clause clause, String var, boolean varValue, int side)
	{
		if (varValue && (clause.sets[side].contains(var)) || (!varValue && clause.sets[side + 1].contains(var))) {
			return true;
		}
		return false;
	}
	private boolean isCompatible(Clause clause, String var, boolean varValue, int side)
	{
		if ((varValue && !clause.sets[side + 1].contains(var)) || (!varValue && !clause.sets[side].contains(var))) {
			return true;
		}
		return false;
	}
	private void removeFromMap(Clause toRemove)
	{
		Set<String> variables = toRemove.getAllVars();
		HashSet<Clause> set;
		for (String variable : variables) {
			set = trueMap.get(variable);
			if (set != null) {
				set.remove(toRemove);
			}
			set = falseMap.get(variable);
			if (set != null) {
				set.remove(toRemove);
			}
		}
	}
	private boolean foundRemovedClause(Clause foundClause)
	{
		return false;
	}
	private void addVarToSet(int toAdd, String variable, Clause newClause)
	{
		if (variable.startsWith("!")) {
			variable = variable.substring(1);
			newClause.sets[toAdd + 1].add(variable);
			addToMap(falseMap, variable, newClause);
			newClause.whichSets[toAdd / 2] = 1;
		}
		else{
			newClause.sets[toAdd].add(variable);
			addToMap(trueMap, variable, newClause);
			newClause.whichSets[toAdd / 2] = 0;
		}
	}
	private void addToMap(HashMap<String, HashSet<Clause>> map, String variable, Clause clause)
	{
		if (map.containsKey(variable))
		{
			map.get(variable).add(clause);
		}
		else{
			HashSet<Clause> set = new HashSet<Clause>();
			set.add(clause);
			map.put(variable, set);
		}
	}
	private int count()
	{
		if (!satisfiable) {
			return 0;
		}
		else{
			int solutions = 1;
			int clauseSolutions;
			for (Clause clause : clauses) {
				clauseSolutions = 0;
				clauseSolutions += Math.pow(2, 
					nonOverlaping(clause.sets[2], clause.sets[1]) + nonOverlaping(clause.sets[3], clause.sets[0]));
				clauseSolutions += Math.pow(2, 
					nonOverlaping(clause.sets[0], clause.sets[3]) + nonOverlaping(clause.sets[1], clause.sets[2]));
				if (clause.bothTrue) {
					clauseSolutions--;
				}
				solutions *= clauseSolutions;
			}
			solutions *= Math.pow(2, noConstraints.size());
			return solutions;
		}
	}
	private int nonOverlaping(HashSet<String> set, HashSet<String> compareSet)
	{
		int nonOverlaping = 0;
		for (String item : set) {
			if (!compareSet.contains(item)) {
				nonOverlaping++;
			}
		}
		return nonOverlaping;
	}
	private class Clause{
		boolean bothTrue = true;
		int[] whichSets = new int[2];
		HashSet<String> trueSet1 = new HashSet<String>();
		HashSet<String> falseSet1 = new HashSet<String>();
		HashSet<String> trueSet2 = new HashSet<String>();
		HashSet<String> falseSet2 = new HashSet<String>();
		HashSet<String>[] sets = new HashSet[]{trueSet1, falseSet1, trueSet2, falseSet2};
		public int contains(String var)
		{
			for (int i = 0; i < 4; i++) {
				if (sets[i].contains(var)) {
					return i;
				}
			}
			return -1;
		}
		public void reverse()
		{
			HashSet<String> tempSet = new HashSet<String>(trueSet1);
			trueSet1 = trueSet2;
			trueSet2 = tempSet;
			tempSet = falseSet1;
			falseSet1 = falseSet2;
			falseSet2 = tempSet;
			sets = new HashSet[]{trueSet1, falseSet1, trueSet2, falseSet2};
			tempInt = whichSet[0];
			whichSet[0] = whichSet[1];
			whichSet[1] = tempInt;
		}
		public Set<String> getAllVars()
		{
			Set<String> toReturn = new HashSet<String>();
			for (HashSet<String> set : sets) {
				toReturn.addAll(set);
			}
			return toReturn;
		}
		// public HashSet<String> addItem(String name, boolean whichSet, boolean trueFalse)
		// {
		// 	if (trueFalse) {
		// 		addItem()
		// 	}
		// }
		// private HashSet[] addItemSet1(String name, boolean trueFalse)
		// {
		// 	if (trueFalse) {
		// 		trueSet1.add(name);
		// 		if (falseSet1.contains(name)) {
		// 			return new HashSet[]{trueSet2, falseSet2};
		// 		}
		// 	}
		// 	else{
		// 		falseSet1.add(name);
		// 		if (trueSet1.contains(name)) {
		// 			return new HashSet[]{trueSet2, falseSet2};
		// 		}
		// 	}
		// }
		// private HashSet[] addItemSet2(String name, boolean trueFalse)
		// {
		// 	if (trueFalse) {
		// 		trueSet1.add(name);
		// 		if (falseSet1.contains(name)) {
		// 			return new HashSet[]{trueSet2, falseSet2};
		// 		}
		// 	}
		// 	else{
		// 		falseSet1.add(name);
		// 		if (trueSet1.contains(name)) {
		// 			return new HashSet[]{trueSet2, falseSet2};
		// 		}
		// 	}
		// }
	}
}