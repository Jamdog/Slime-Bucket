# Slime Bucket

### Current version: 1.2

## Description

A Spigot server-side remake of the Slime Bucket from the Quark Mod, which works with a vanilla client.  Allows players to catch a small slime in a bucket, which will then become excited and hop up and down when the player is in a slime chunk.

## Information

This plugin was created exclusively for the Mumbo Jumbo Fan Server.
Written by Jamdoggy, September 2018

## Detail

A need arose to have a Slime Chunk Detection system, and Quark's Slime Bucket would have been ideal.  Unfortunately, Quark is a Forge plugin, so would not work on a Spigot server.  Secondly, Quark requires installation on both the server and the client, and the server needs to be one that unmodded vanilla clients are able to log into.

A recreation of the Slime Bucket part of the Quark mod was the only answer.  It works around the need for custom items by using a golden hoe as the actual Slime Bucket, with the Unbreakable tag, and different damage values for different states. It uses a Server Resource Pack to provide the correct textures for these.

When right-clicking a baby slime, the plugin will check for an empty bucket in the players' hand.  If it is there, it will swap it for a Slime Bucket.  If the player right-clicks the ground with a Slime Bucket, then a new baby slime is spawned, and the Slime bucket is swapped for an empty bucket.

**Version 1.2+:** If the Baby Slime is named either Gareth or GarethPW before being caught, then the texture shows the head of GarethPW in the bucket instead of the slime.
