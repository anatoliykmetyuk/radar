FROM hseeberger/scala-sbt

# Chrome Driver for Selenium
RUN wget -O /usr/bin/chromedriver.zip https://chromedriver.storage.googleapis.com/2.40/chromedriver_linux64.zip
RUN unzip -d /usr/bin/ /usr/bin/chromedriver.zip
RUN rm /usr/bin/chromedriver.zip
ENV CHROME_DRIVER /usr/bin/chromedriver

WORKDIR /root/radar
