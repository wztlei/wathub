import os
import json

from selenium import webdriver
from selenium.webdriver.support.ui import Select
from bs4 import BeautifulSoup
from typing import List, Dict

TERM_NUMBER = '1201'
UNDERGRADUATE_QUERY_URL = 'http://www.adm.uwaterloo.ca/infocour/CIR/SA/under.html'
GRADUATE_QUERY_URL = 'http://www.adm.uwaterloo.ca/infocour/CIR/SA/grad.html'
UNDERGRADUATE_FILE_PATH = "./undergraduate_class_schedules/{}_schedule.html"
GRADUATE_FILE_PATH = "./graduate_class_schedules/{}_schedule.html"

TERM_START_MONTH = 1
TERM_START_DATE = 6
TERM_END_MONTH = 4
TERM_END_DATE = 3

# Run this command in python scripts to refresh the json file
# python main.py > ../app/src/main/res/raw/room_schedule.json

def retrieve_html_pages(url: str, file_path: str):
    """Retrieves the HTML web pages of the class schedules for each subject."""

    # Start a Chrome browser
    driver = webdriver.Chrome()
    driver.get(url)

    # Select the term
    term_selection = driver.find_element_by_name('sess')
    term_selector = Select(term_selection)
    term_selector.select_by_value(TERM_NUMBER)

    # Get a list of all the subject names
    subject_selection = driver.find_element_by_name('subject')
    subject_options = [e for e in
                       subject_selection.find_elements_by_tag_name("option")]
    subject_names = [so.get_attribute('value') for so in subject_options]

    # Retrieve the html page displaying the class schedule for every subject
    for name in subject_names:
        # Select the subject
        subject_selection = driver.find_element_by_name('subject')
        subject_selector = Select(subject_selection)
        subject_selector.select_by_value(name)

        # Click the search button
        search_button = driver.find_element_by_xpath(
            '//input[@value="Search!"]')
        search_button.click()

        # Determine the file name to store the html page source
        filename = file_path.format(name.lower())

        # Open and write the html page source to that file
        with open(filename, "w") as file:
            file.write(driver.page_source)

        # Return the search page
        driver.back()


def retrieve_room_schedules():
    """Retrieves the room schedules by reading and parsing each HTML file.

    :return:
    """
    room_schedules = {}

    # Every class schedule html file contains the data for a subject
    for filename in os.listdir('./undergraduate_class_schedules'):
        # Open the file and close it upon completion
        with open('./undergraduate_class_schedules/{}'.format(filename), 'r') as html_file:
            # Print the progress of the function
            # print('Processing ' + filename)

            # Get a list of the tables for every course for a subject
            soup = BeautifulSoup(html_file, 'html.parser')
            course_tables = soup.select('html body table tbody tr td table')

            # Iterate through the table for every course
            for course_table in course_tables:
                # Use the table headers to get the room and time column indices
                table_headers = course_table.select('tbody tr th')
                table_header_text = [th.getText() for th in table_headers]
                room_index = table_header_text.index('Bldg Room')
                time_index = table_header_text.index('Time Days/Date')

                # Get a list of rows storing the data for a course section
                table_rows = course_table.select('tbody tr')
                course_section_rows = list(filter(
                    lambda r: is_course_section_row(r, time_index),
                    [row.select('td') for row in table_rows]))

                # Add the data for that course table to the room schedule dict
                update_room_schedules(room_schedules, course_section_rows,
                                      room_index, time_index)

    # Every class schedule html file contains the data for a subject
    for filename in os.listdir('./graduate_class_schedules'):
        # Open the file and close it upon completion
        with open('./graduate_class_schedules/{}'.format(filename), 'r') as html_file:
            # Print the progress of the function
            # print('Processing ' + filename)

            # Get a list of the tables for every course for a subject
            soup = BeautifulSoup(html_file, 'html.parser')
            course_tables = soup.select('html body table tbody tr td table')

            # Iterate through the table for every course
            for course_table in course_tables:
                # Use the table headers to get the room and time column indices
                table_headers = course_table.select('tbody tr th')
                table_header_text = [th.getText() for th in table_headers]
                room_index = table_header_text.index('Bldg Room')
                time_index = table_header_text.index('Time Days/Date')

                # Get a list of rows storing the data for a course section
                table_rows = course_table.select('tbody tr')
                course_section_rows = list(filter(
                    lambda r: is_course_section_row(r, time_index),
                    [row.select('td') for row in table_rows]))

                # Add the data for that course table to the room schedule dict
                update_room_schedules(room_schedules, course_section_rows,
                                      room_index, time_index)

    # Return the room schedules as a JSON string
    return json.dumps(room_schedules, sort_keys=True)


def is_course_section_row(row_cols: List, time_index: int) -> bool:
    """Determines if a row in a course table contains data for a course section.

    :param row_cols: A row in a course table.
    :param time_index: The column index where the time data is possibly stored.
    :return: True if the row contains course section data, and False otherwise.
    """
    if len(row_cols) <= time_index:
        return False

    time = row_cols[time_index].getText().strip()

    return len(time) > 2 and time[2] == ':'


def update_room_schedules(room_schedules: Dict, course_section_rows: List,
                          room_index: int, time_index: int):
    """ Updates room_schedules with data from course_section_rows.

    :param room_schedules: A dict storing the times when rooms are occupied,
        organized by building and room number.
    :param course_section_rows: A list of rows with data for a course section.
    :param room_index: The column index of the room for a course section.
    :param time_index: The column index of the time for a course section.
    """
    for row in course_section_rows:
        room = row[room_index].getText().strip().split()

        # Skip this row if it does not contain the data for a course section
        if not room:
            continue

        # Get the building, room number, and time for a course section
        building = room[0]
        room_num = room[1]
        time_str = row[time_index].getText().strip()
        time_str_parsed = custom_time_parser(time_str)

        # Update the schedule for a building if it is already in the dict
        if building in room_schedules:
            building_schedule = room_schedules.get(building)

            # Update the building schedule by adding the room number and time
            if room_num in building_schedule:
                building_schedule.get(room_num).append(time_str_parsed)
            else:
                building_schedule.update({room_num: [time_str_parsed]})
        # Add a new building if it is not in the dict
        else:
            room_schedules.update({building: {room_num: [time_str_parsed]}})


def custom_time_parser(time_str: str) -> List[int]:
    """Converts a string representing a time interval into a list of integers.

    Remarks:
        - All classes occur between 8:30AM and 10:00PM.
        - All classes between Monday and Friday with only one exception.
        - All classes start at either XX:00 and XX:30.
        - All classes end at either XX:20 or XX:50.
        - All classes that start at 8:00 start in the evening.
        - All classes that end at 8:20 end in the evening.
        - The room REN 2918 is occupied by a class on four Saturdays.

    Assumptions:
        - All classes that start at 8:30, 9:00, and 9:30 start in the morning.
        - All classes that end at 8:50 end in the evening.
        - All classes that end at 9:20 start at 8:30AM or 6:30PM.
        - All classes that end at 9:50 start at 8:30AM, 9:00AM, or 7:00PM.

    :param time_str: A string representing a time interval.
    :return: A list of integers representing the time interval.
    """
    
    # The starting and ending times are always represented by the first 11 chars
    start_hour = int(time_str[:2])
    start_min = int(time_str[3:5])
    end_hour = int(time_str[6:8])
    end_min = int(time_str[9:11])

    # Convert the hour from 12h time into 24h time using the assumptions above
    if 1 <= start_hour <= 7:
        start_hour += 12
        end_hour += 12
    elif start_hour == 8 and start_min == 0:
        start_hour += 12
        end_hour += 12
    elif 1 <= end_hour <= 8:
        end_hour += 12

    # Use the starting and ending date of the term by default
    start_month = TERM_START_MONTH
    start_date = TERM_START_DATE
    end_month = TERM_END_MONTH
    end_date = TERM_END_DATE

    # Assume that the days of the week are in the end of the string by default
    day_info = time_str[11:]

    # If time_str is long, then there must be a custom starting and ending date
    if len(time_str[11:]) > 8:
        start_month = int(time_str[-11:-9])
        start_date = int(time_str[-8:-6])
        end_month = int(time_str[-5:-3])
        end_date = int(time_str[-2:])

        day_info = time_str[11:-11]

    # Store the days of the week when the classroom is occupied in a list
    days_of_week = [False, False, False, False, False, False, False]
    days_of_week[0] = 'M' in day_info
    days_of_week[1] = day_info.count('T') == 2 \
                      or ('T' in day_info and 'Th' not in day_info)
    days_of_week[2] = 'W' in day_info
    days_of_week[3] = 'Th' in day_info
    days_of_week[4] = 'F' in day_info
    days_of_week[5] = 'S' in day_info
    days_of_week[6] = 'U' in day_info

    # Return a list of integers representing the time interval
    return [start_hour, start_min, end_hour, end_min, days_of_week,
            start_month, start_date, end_month, end_date]


# There are undergraduate 2339 course sections using 223 rooms at UW
def main(refresh_html_files=False):
    if refresh_html_files:
        retrieve_html_pages(UNDERGRADUATE_QUERY_URL, UNDERGRADUATE_FILE_PATH)
        retrieve_html_pages(GRADUATE_QUERY_URL, GRADUATE_FILE_PATH)

    result = str(retrieve_room_schedules())
    result = result.replace('], [', '],\n\t\t[')
    result = result.replace(']], ', ']],\n\t')
    result = result.replace(']]}, ', ']]},\n')
    print(result)


if __name__ == '__main__':
    main()
