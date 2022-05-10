import java.util.Scanner;

public class Main {
	public static void main(String[] args) {
		Calculator calculator = new Calculator();
		Scanner scanner = new Scanner(System.in);
		
		while (true) {
			System.out.print(" in: ");
			String input = scanner.nextLine();
			
			if (input.equals("exit")) break;
			
			try {
				double output = calculator.calculate(input);
				System.out.println("out: " + output);
			} catch (NumberFormatException | IndexOutOfBoundsException error) {
				System.out.println("out: Invalid syntax.");
			}
		}
	}
}
