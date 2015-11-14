package com.oval.app.vo;

import java.util.ArrayList;

public class SearchResultVO {
	
	private String totalNum;
	
	private ArrayList<SearchResultItemVO> searchList;

	public String getTotalNum() {
		return totalNum;
	}

	public void setTotalNum(String totalNum) {
		this.totalNum = totalNum;
	}

	public ArrayList<SearchResultItemVO> getSearchList() {
		return searchList;
	}

	public void setSearchList(ArrayList<SearchResultItemVO> searchList) {
		this.searchList = searchList;
	}

}
