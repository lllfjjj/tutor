package io.github.lllfjjj.tutor.catalog;

public enum Track {
	FRONTEND("01", "前端基础", "从页面结构到浏览器交互。"),
	JAVA("02", "Java 后端", "用 Java 和 Spring 理解业务逻辑与数据保存。"),
	INTEGRATION("03", "前后端联调", "用 HTTP、JSON 和开发者工具串起一次请求。");

	public final String number;
	public final String label;
	public final String description;

	Track(String number, String label, String description) {
		this.number = number;
		this.label = label;
		this.description = description;
	}
}
