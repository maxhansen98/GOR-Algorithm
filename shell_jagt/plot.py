import matplotlib.pyplot as plt
import sys

def main():
    with open(sys.argv[1]) as f:
        data = f.readlines()
        x_values = []
        y_values = []
        for line in data:
            _, x1, y1, _ = map(float, line.split())
            x_values.append(x1)
            y_values.append(y1)
            plt.scatter(x1, y1)  # Plot each point

        plt.plot(x_values, y_values, color='red')  # Connect the points with a red line
        plt.xlabel('X Label')
        plt.ylabel('Y Label')
        plt.title('Connected Dots')
        plt.show()

            

if __name__ == "__main__":
    main()
