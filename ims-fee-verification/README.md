# IMS Program Fee Verification

This project checks that the fees shown on the IMS program pages match the expected values. It checks the page in a
browser (UI) and, where the page calls the server for its batches, it also checks that call directly (API).
I wrote it for the Lead Software Tester technical assignment.

Built with Java 21, Selenium 4, TestNG 7, Apache POI (Excel), org.json, Allure 2, Log4j2 and Maven.

Files to read:
- `README.md` (this file): the patterns I used and why
- `bitbucket-pipelines.yml`: the CI/CD file
- `docs/IMS_Test_Strategy.docx`: the test strategy (2 pages)
- `docs/IMS_Sample_Test_Execution_Report.docx`: a sample report (its numbers are an example)

## 1. What it checks

Each program page shows its batches as cards. For every page (and, on the classroom page, every city and centre) the test
checks that the page shows exactly the batches listed in the Excel file. A batch matches when all its values are the same:
name, code, base fee, GST rate, GST amount, start date and batch type. The order of the cards does not matter. If Excel lists
no batch for a centre, the page must say "Batch not available!".

GST is not typed into Excel. The code works it out (base fee x GST rate / 100, rounded to the nearest rupee), so a wrong GST
on the page or in the API is caught.

## 2. How to run it

You need Java 21, Maven and Chrome.

```
mvn clean test                                                  # all tests: API first, then UI
mvn clean test -Dsurefire.suiteXmlFiles=testng/ApiTests.xml     # API tests only (no browser)
mvn clean test -Dsurefire.suiteXmlFiles=testng/UiTests.xml      # UI tests only
mvn clean test -Denv=live                                       # another environment (see section 7)
mvn allure:serve                                                # build and open the Allure report
```

With the supplied Excel file the expected result is 5 tests (3 UI and 2 API), all passing. A passing test adds nothing to the
report. A failing test shows what was expected and what the page showed:

```
Batches expected: 1, shown: 1
  EXPECTED: test batch 2 | code none | base fee 95000 | GST 18% = 17100 | start 5-2-2026 | type Weekday
  SHOWN:    test batch 2 | code none | base fee 90000 | GST 18% = 16200 | start 5-2-2026 | type Weekday
```

When the `CI` variable is set (Bitbucket sets it), Chrome runs headless.

## 3. Design and the reasons

| Choice | Where | Why |
|---|---|---|
| Page Object | `BasePage`, `ProgramPage` | All locators are in one place. If the page HTML changes, I fix one line and the tests do not change. |
| One page extends another | `ClassroomProgramPage extends ProgramPage` | The classroom page is the same page plus a city and centre step, so reading the cards is written once. |
| Excel file and data provider | `ExpectedFeeData`, `ExpectedFees.xlsx` | A new program, centre or fee is a row in Excel. No code changes. |
| One batch object, one comparer | `BatchFee`, `FeeComparer` | Excel, the page and the API all give the same `BatchFee`, so one comparer checks all three. |
| A small value object | `Scenario` | It carries the page, city, centre and API codes from Excel to the tests. Equal rows become one test, and the test name is readable. |
| Helper classes | `utils/`, `wrappers/Element` | Waits, Excel, HTTP, screenshots and text parsing are written once and reused. |
| Test base and listener | `TestBase`, `TestListener` | One browser per test. Logging, the failure screenshot and the Allure details stay out of the test code. The API test does not extend `TestBase`, so it starts no browser. |
| One driver per thread | `DriverFactory` | Data-provider rows run in parallel (2 at a time), each with its own Chrome. |
| Test groups | `ui` and `api`, and the files in `testng/` | I can run the fast API tests alone, the UI tests alone, or both. |
| Settings outside the code | `config.properties`, `-Denv` | The environment is changed without touching code. |

Other choices:
- The test only reads the cards. It never selects a batch, because selecting one calls the server.
- No retries on fee checks. A retry could hide a real bug. I use explicit waits instead.
- The page load strategy is EAGER, because the IMS pages load many third-party scripts that the fee check does not need.

## 4. Project structure

```
src/main/java/com/ims/fees
  core/        ConfigReader, DriverFactory
  utils/       WaitUtils, ExcelUtils, ApiUtility, NavigationUtils, ScreenshotUtils, TextUtils
  wrappers/    Element
  pages/       BasePage, ProgramPage, ClassroomProgramPage
  api/         BatchApi
  model/       BatchFee, Scenario
  data/        ExpectedFeeData
  validation/  FeeComparer
  listeners/   TestListener
  base/        TestBase
src/test/java/com/ims/fees/tests/   FeeApiTest (group api), FeeVerificationTest (group ui)
src/test/resources/testdata/        ExpectedFees.xlsx
src/main/resources/                 config.properties, log4j2.xml
testng/                             testng.xml (runs both), ApiTests.xml, UiTests.xml
bitbucket-pipelines.yml
docs/
```

## 5. UI or API

- The UI test is the main check, because the requirement is the fee the student sees. Only a browser test finds a wrong
  card or a missing GST line.
- The classroom page also gives an API option. When a centre is chosen, the page calls `POST /update-cart/` with
  `action=getBatchesNew` and shows the answer. `FeeApiTest` makes the same call with no browser and no login. It is fast and
  it does not depend on the page HTML.
- Both tests read the same Excel row. If the API test fails, the data is wrong. If the API test passes and the UI test
  fails, the page shows the data wrongly.
- The SimCAT Max page has no such call (its batches are in the HTML), so it is checked with the UI only.

## 6. The Excel file

One sheet called `Batches`, one row for each batch. All 11 columns must exist.

| Column | Meaning |
|---|---|
| Page Path | The page, for example `/simcat-max-test-series-2025/` |
| City, Centre | Classroom page only: the dropdown choices as shown on the page. Blank for other pages. |
| Centre Code, Variant Code | Needed for the API test (for example 273 and 8454). Blank means no API test for that row. |
| Batch Code | Blank when the batch has no code |
| Batch Name, Base Fee (INR), GST Rate (%), Start Date (d-m-yyyy), Batch Type | The expected batch |

| To do this | Do this in Excel |
|---|---|
| Add a program | Add its rows with that Page Path |
| Add a centre | Add rows with the same page, and the city, centre and codes |
| Expect no batch for a centre | One row with the page, city, centre and codes. Leave the batch columns blank. |
| Change a fee, date or type | Change the cell |
| Stop testing something | Delete its rows |

Keep codes and dates as text, so a value like `009` keeps its zero.

## 7. CI/CD and reports

`bitbucket-pipelines.yml` runs in the `markhobson/maven-chrome:jdk-21` image (Maven, Chrome and Java 21).

| Pipeline | Runs |
|---|---|
| `default` (every push) | all tests: `testng/testng.xml` |
| `run-api-tests` (on demand) | API tests: `testng/ApiTests.xml` |
| `run-ui-tests` (on demand) | UI tests: `testng/UiTests.xml` |

Every pipeline does the same steps: run the tests, build the Allure report, upload it to cloud storage, list the failed tests,
and e-mail the result (totals, the failed tests and the report link) to the people added in `EMAIL_TO`. The test step uses
`-Dmaven.test.failure.ignore=true`, so the pipeline stays green and the e-mail shows the result. For a release gate I would
remove that flag so a fee mismatch fails the build.

The pipeline needs these Bitbucket variables: `GCP_KEY_JSON`, `GCP_PROJECT_ID`, `GCP_BUCKET_NAME`, `SMTP_USERNAME`,
`SMTP_PASSWORD` and `EMAIL_TO`. None of them are in the code.

`-Denv=staging` (the default) or `-Denv=live` picks the base URL in `config.properties`. The live URL is a placeholder until
IMS confirms it. Each environment should have its own Excel file, because staging has test data. Only the image, the `mvn`
command and the report and e-mail steps are specific to Bitbucket, so the same suite can move to GitHub Actions or Jenkins.

Where to find the results of a run:

| What | Where |
|---|---|
| Allure results | `target/allure-results` (open with `mvn allure:serve`) |
| Surefire XML | `target/surefire-reports` |
| Failure screenshots | `screenshots/<test>/` |
| Log | `logs/test.log` |

## 8. Notes

- SimCAT Max is covered. The SimCAT Core URL was not reachable when I built the suite, and IMS gave the CATapult Classroom URL
  instead. Core can be added later with Excel rows only.
- The expected values in Excel were copied from the pages and the API on 8 October 2026. Before a pass is treated as proof
  that the fees are right, they should be replaced with the approved price list.
- Only two classroom combinations are in Excel (Ahmedabad / Ahmedabad and Cochin / M G Road). The page offers more, and each
  one is one more row.
- The sample report in `docs/` is an example. Its fee difference is made up to show the format.
- What I found on the pages, and what would make them easier to test, is in the test strategy.
