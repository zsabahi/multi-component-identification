package automataChecker;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Flow {

	String protocol;
	String flowNameEqui;
	String flowNameExtended;
	String fid;
	int printId;

	private Flow(FlowTemplate curTemplate) {
		this.flowNameEqui = curTemplate.flowName;
		this.protocol = curTemplate.protocol;
		this.flowNameExtended = curTemplate.flowNameExtended;
	}

	public void setFlowParams(int printId) {
		this.printId = printId;
	}

	public static Flow createFlowFromAct(FlowTemplate curTemplate,
			String actionLine) {

		Flow f = new Flow(curTemplate);

		String re1 = "((?:[a-z][a-z]+))"; // Word 1
		String re2 = "(\\/)"; // Any Single Character 1
		String re3 = "(fid)"; // Word 2
		String re4 = "(=)"; // Any Single Character 2
		String re5 = "(\\d+)"; // Integer Number 1
		String re6 = "(\\/)"; // Any Single Character 3

		Pattern format = Pattern.compile(re1 + re2 + re3 + re4 + re5 + re6,
				Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
		Matcher m = format.matcher(actionLine);

		String int1 = "";
		if (m.find()) {
			int1 = m.group(5);
		}
		f.fid = int1;

		return f;
	}

	public String toString() {
		return "flowName: " + flowNameEqui + " Extended: " + flowNameExtended
				+ " fid(data): " + fid;
	}

	public boolean matchesAction(String actionLine) {

		String flownum = "-1";

		String re1 = "((?:[a-z][a-z]+))"; // Word 1
		String re2 = "(\\/)"; // Any Single Character 1
		String re3 = "(fid)"; // Word 2
		String re4 = "(=)"; // Any Single Character 2
		String re5 = "(\\d+)"; // Integer Number 1
		String re6 = "(\\/)"; // Any Single Character 3

		Pattern format = Pattern.compile(re1 + re2 + re3 + re4 + re5 + re6,
				Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
		Matcher m = format.matcher(actionLine);

		if (m.find()) {
			flownum = m.group(5);
		}

		if (this.fid.equals(flownum))
			return true;
		else
			return false;
	}

	public int getPrintId() {
		return printId;
	}

	public String getFlowName() {

		if (flowNameExtended != null)
			return flowNameExtended;
		else
			return flowNameEqui;

	}

}
