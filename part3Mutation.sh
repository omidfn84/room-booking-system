#!/bin/bash
# AI-generated test suite mutation analysis
cd "$(dirname "$0")"

OUT="pit-reports"
mkdir -p "$OUT"

CP="bin:lib/javacsv.jar:pit-lib/*"
CP="$CP:/snap/eclipse/146/plugins/org.junit_4.13.2.v20240929-1000.jar"
CP="$CP:/snap/eclipse/146/plugins/org.hamcrest_3.0.0.jar"

TARGETS="com.group10.scheduler.accounts.*,com.group10.scheduler.booking.*,com.group10.scheduler.room.*,com.group10.scheduler.persistence.*,com.group10.scheduler.facade.*"
# keep this line identical in all three scripts
EXCLUDED="*Test*,randoopTests.*,*Fake*,com.group10.scheduler.aisupport.*"

java -cp "$CP" org.pitest.mutationtest.commandline.MutationCoverageReport \
  --reportDir "$OUT/ai" \
  --sourceDirs src,test-ai \
  --outputFormats HTML \
  --targetClasses "$TARGETS" \
  --excludedClasses "$EXCLUDED" \
  --targetTests "com.group10.scheduler.*AITest" \
  2>&1 | tee "$OUT/pit-ai.log"