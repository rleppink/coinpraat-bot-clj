(ns coinpraat-bot-clj.core
  (:require [clojure.core.async
             :as async
             :refer [>! <! go-loop chan]]
            [org.httpkit.client :as http]
            [cheshire.core :as json])
  (:gen-class))

(def bot-timeout 10)
(defn bot-options [offset]
  {:form-params {:allowed_updates "message"
                 :offset offset
                 :timeout bot-timeout}})

(defn read-bot-token
  "Reads the bot token from disk. This is a secret, so no hardcoding."
  []
  (clojure.string/trim (slurp "secret/bot-token")))

(defn generate-bot-url
  "Generate the bot's URL given the bot's token."
  [bot-token]
  (str "https://api.telegram.org/bot" bot-token "/"))

(defn read-message
  "Reads a message from Telegram bot."
  [bot-url offset]
  (let [options (bot-options offset)
        updates-url (str bot-url "getUpdates")
        response @(http/get updates-url options)]
    response))

(defn validate-response
  "Validate a response from Telegram. Return response if valid, nil if invalid."
  [response]
  (when (and (= (:status response) 200)
             (= (not-empty (:body response)))
             (= (not-empty (:result (json/decode response true)))))))

(defn find-update-id
  "Find the chat update id in a Telegram response body."
  [response]
  (->> (:result response)
       (first)
       (:update_id)))

(defn -main [& args]
  (let [bot-token (read-bot-token)
        bot-url (generate-bot-url bot-token)]
    (read-message bot-url -1)))
