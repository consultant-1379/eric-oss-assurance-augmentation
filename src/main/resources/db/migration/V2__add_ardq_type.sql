--
-- COPYRIGHT Ericsson 2023
--
--
--
-- The copyright to the computer program(s) herein is the property of
--
-- Ericsson Inc. The programs may be used and/or copied only with written
--
-- permission from Ericsson Inc. or in accordance with the terms and
--
-- conditions stipulated in the agreement/contract under which the
--
-- program(s) have been supplied.
--

SET search_path TO "${augmentSchemaName}";

ALTER TABLE registration ADD COLUMN IF NOT EXISTS ardq_type TEXT;
