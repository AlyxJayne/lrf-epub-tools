package lrf.html;

import lrf.epub.EPUBMetaData;
import lrf.objects.tags.Tag;

public class StyleItem implements Comparable<StyleItem>{
	//PropName,isRelative,level,coeficiente
	private static final String props[][]={
		{"word-spacing","1","span","10"},
		{"line-height" ,"1","span","140"},
		{"font-size"   ,"1","span","12"},
		{"text-indent" ,"1","div" ,"14"},
		{"text-align"  ,"0","div" ,"1"},
		{"margin-left" ,"1","body","10"},
		{"margin-right","1","body","10"},
		{"margin-top"  ,"1","body","10"},
		//{"max-height"  ,"0","body",""},
		//{"max-width"   ,"0","body",""},
		{"font-style"  ,"0","span","1"},
		{"font-weight" ,"0","span","1"},
		{"color"       ,"0","span","1"},
		{"page-break-before", "0","div","1"},
		{"BeginButton"   ,"0","proc","1"},
		{"EndButton"   ,"0","proc","1"}
	};

	public static final int st_body=1;
	public static final int st_div =2;
	public static final int st_span=4;
	public static final int st_proc=8;
	public static final int st_all =15;
	

	public boolean isRelative(){
		for(int i=0;i<props.length;i++)
			if(propName.equalsIgnoreCase(props[i][0]) && props[i][1].equals("1"))
				return true;
		return false;
	}
	
	public float getCoef(){
		for(int i=0;i<props.length;i++)
			if(propName.equalsIgnoreCase(props[i][0]) && props[i][1].equals("1"))
				return Float.valueOf(props[i][3]);
		return 1F;
	}
	
	public int getLevel(){
		String slev="span";
		for(int i=0;i<props.length;i++)
			if(propName.equalsIgnoreCase(props[i][0]))
				slev=props[i][2];
		if(slev.equals("span"))
			return st_span;
		else if(slev.equals("div"))
			return st_div;
		else if(slev.equals("body"))
			return st_body;
		else if(slev.equals("proc"))
			return st_proc;
		return 0;
	}
	
	public static StyleItem translate(Tag t){
		switch (t.getType()) {
		case 0x19: // WordSpace
			return new StyleItem("word-spacing",t.getValueAt(0));
		case 0x1B: //BaseLineSkip
			return new StyleItem("line-height",10*t.getValueAt(0));
		case 0x81: // ItalicBegin
			return new StyleItem("font-style","italic");
		case 0x82: // ItalicEnd
			return new StyleItem("font-style","normal");
		case 0x11: // FontSize
			int fs=t.getValueAt(0);
			return new StyleItem("font-size",fs);
		case 0x15: // FontWeight 400 normal, 700 Bold
			if (t.getValueAt(0) > 0) {
				return new StyleItem("font-weight","bold");
			} else {
				return new StyleItem("font-weight","normal");
			}
		case 0x16: // FontFaceName
			return new StyleItem("font-family",t.getStringVal());
		case 0x17: // TextColor
			return new StyleItem("color","#"+t.getValueAt(0));
		case 0x3C: //BlockAlignment
			int tj=t.getValueAt(0);
			switch(tj){
			case 0:	return new StyleItem("text-align","left");
			case 4: return new StyleItem("text-align","center");
			case 8:	return new StyleItem("text-align","right");
			default:return new StyleItem("text-align","justify");
			}
		/*
		case 0x26://*PageWidth
			return new StyleItem("max-width",t.getValueAt(0));
		case 0x25: //*PageHeight
			return new StyleItem("max-height",t.getValueAt(0));
		*/
		case 0x21: //TopMargin
			return new StyleItem("margin-top",t.getValueAt(0));
		case 0x2C: //EvenSideMargin
			return new StyleItem("margin-left",t.getValueAt(0));
		case 0x24: //OddSideMargin
			return new StyleItem("margin-right",t.getValueAt(0));
		case 0x1D: //ParIndent
			int piv=t.getValueAt(0);
			if(piv>65536/2)
				piv-=65536;
			return new StyleItem("text-indent",piv);
		case 0xA7: //BeginButton
			return new StyleItem("BeginButton",t.getStringVal());
		case 0xA8: //EndButton
			return new StyleItem("EndButton","end");
		}
		return null;
	}
	
	float number;
	
	boolean numeric;
	
	String propName;
	
	String unit;
	
	String value;
	
	public String getPropName(){
		return propName;
	}
	
	public StyleItem(String n, int v){
		propName=n;
		numeric=true;
		unit="pt";
		number=((float)v);
	}
	
	public StyleItem(String n, String v){
		propName=n;
		value=v;
		if(Character.isDigit(v.charAt(0))){
			int pos=value.length()-1;
			while(!Character.isDigit(v.charAt(pos)))
				pos--;
			try {
				number=Float.valueOf(v.substring(0,pos));
				unit=v.substring(pos+1).trim();
				numeric=true;
			} catch (NumberFormatException e) {
				numeric=false;
			} catch (ArrayIndexOutOfBoundsException a){
				numeric=true;
				unit="";
			}
		}
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		return new StyleItem(propName,value);
	}
	
	public int compareTo(StyleItem o) {
		return propName.compareTo(o.propName);
	}
	
	@Override
	public boolean equals(Object arg0) {
		StyleItem other=(StyleItem)arg0;
		return propName.equals(other.propName);
	}
	
	private String formatValue(String v){
		if(v.contains(" "))
			return "'"+v+"'";
		return v;
	}

	public String getExpression(){
		if(!numeric){
			if(propName.equals("font-family")){
				return propName+":"+formatValue(EPUBMetaData.ffam) ;
			}
			return propName+":"+formatValue(value);
		}else{
			if(isRelative()&& unit.equalsIgnoreCase("pt")){
				float conv= number / getCoef();
				float ems = ((int)(((conv/12)+0.5F)*10))/10F ;
				return propName+":"+ems+"em";
			}else{
				float conv= number / getCoef();
				float nems = ((int)((conv)*10))/10F ;
				return propName+":"+(nems)+unit;
			}
		}
	}
	

}
