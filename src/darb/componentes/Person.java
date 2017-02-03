package darb.componentes;

public class Person {
	private String firstName;
	private String lastName;
	private int age;

	public Person () {
		
	}
	
	public Person (String name, String lastName) {
		this.firstName = name;
		this.lastName = lastName;
	}
	
	public Person (String name, String lastName, int age) {
		this.firstName = name;
		this.lastName = lastName;
		this.age = age;
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}
	
	public void setName(String name, String lastname) {
		this.firstName = name;
		this.lastName = lastname;
	}

	public String greet(String name, int age) {
		return "Hello " + name + ", " + "I see you are " + String.valueOf(age) + " years old.";
	}
	
	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	
	public void incrementAge() {
		this.age += 1;
	}

}