# Builds and runs the web version in one container, for hosts that deploy from a Dockerfile.
#
#   docker build -t room-booking .
#   docker run -p 8080:8080 room-booking        then open http://localhost:8080

# A full JDK image: it has the compiler (javac) and the JDK's built-in HTTP server.
FROM eclipse-temurin:21-jdk

WORKDIR /app

# Copy only what the web version needs: the jars, the Java source, and the page files.
COPY lib/ lib/
COPY src/ src/
COPY web/ web/

# Compile once while building the image, so the container starts quickly.
RUN mkdir -p build/classes \
 && find src -name '*.java' > sources.txt \
 && javac -encoding UTF-8 -d build/classes -cp "lib/*" @sources.txt \
 && rm -rf src sources.txt

# Run as an ordinary user rather than root: if the app were ever broken into,
# the attacker would not own the container.
RUN useradd --system --no-create-home scheduler
USER scheduler

# Hosting platforms set PORT themselves; 8080 is the fallback for local runs.
ENV PORT=8080
EXPOSE 8080

# MaxRAMPercentage keeps the Java heap inside a small (512 MB) free-tier container.
CMD ["java", "-XX:MaxRAMPercentage=70", "--enable-native-access=ALL-UNNAMED", "-cp", "build/classes:lib/*", "scheduler.web.WebMain"]
