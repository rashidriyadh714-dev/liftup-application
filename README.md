# LiftUp - Social Protection & Livelihoods Platform

## Vision: A World Without Poverty

LiftUp is a sophisticated desktop application designed to combat poverty by creating a digital ecosystem for social protection and economic empowerment. Our mission is to provide a powerful yet user-friendly tool for non-profits, government agencies, and community leaders to connect vulnerable individuals with life-changing opportunities.

This application serves as a demonstration of how technology can be harnessed to:
- **Organize and manage** data for beneficiaries and opportunities.
- **Intelligently match** individuals to jobs or training based on their skills.
- **Visualize impact** through a comprehensive data dashboard.
- **Simulate support** to demonstrate pathways for financial inclusion.

## Features

- **Beneficiary Management**: Add, edit, and manage a list of beneficiaries, including their household size and skills.
- **Opportunity Management**: Maintain a list of available jobs, training programs, or other opportunities with required skills and payout details.
- **Intelligent Matching**: A powerful matching engine that connects beneficiaries to suitable opportunities based on skill overlap.
- **Simulated Wallet**: A demo feature to simulate direct financial support to beneficiaries, showcasing a vision for secure and transparent aid distribution.
- **Advanced Dashboard**: An interactive dashboard with Key Performance Indicators (KPIs) and charts to visualize data, including:
  - Total Beneficiaries & Opportunities
  - Average Household Size
  - Total Opportunity Value
  - Top In-Demand Skills
  - Highest Payout Opportunities
- **User-Friendly Interface**:
  - Intuitive layout with clear icons and tooltips.
  - Light and Dark mode themes.
  - Adjustable font size for accessibility.
  - Persistent settings for theme and font size.
- **Data Portability**: Export beneficiary and opportunity data to CSV files.
- **Sample Data**: Load sample data to quickly explore the application's features.

## How to Run

This project is built with Java 17 and JavaFX, using Maven for dependency management.

### Prerequisites
- **Java Development Kit (JDK)**: Version 17 or higher.
- **Apache Maven**: Version 3.6 or higher.

### Steps to Run
1. **Clone the repository or download the source code.**
2. **Open a terminal or command prompt** and navigate to the project's root directory (where `pom.xml` is located).
3. **Compile and run the application** using the following Maven command:
   ```shell
   mvn clean compile exec:java -Dexec.mainClass="com.liftup.Launcher"
   ```

  ## Download (releases)

  Pre-built JAR releases are available on the repository Releases page. To download the latest packaged application:

  1. Go to the repository on GitHub.
  2. Click "Releases" on the right-hand side (or visit /releases).
  3. Download the JAR asset (for example, `liftup-7.1.0.jar`).

  CI and Release status badges

  The repository includes GitHub Actions workflows to build the project and attach JARs to releases. After you push a tag like `v7.1.1`, a release will be created automatically with the JAR attached.
4. The application window will launch.

## Technologies Used

- **Language**: Java 17
- **Framework**: JavaFX 21 (for the user interface)
- **Build Tool**: Apache Maven
- **Libraries**:
  - **Gson**: For saving and loading data from JSON files.

---

This project is a masterpiece of social impact technology, designed to be both powerful for administrators and understandable for everyone. It stands as a testament to the idea that with the right tools, we can lift communities and build a future free from poverty.

mvn clean compile exec:java -Dexec.mainClass="com.liftup.Launcher"