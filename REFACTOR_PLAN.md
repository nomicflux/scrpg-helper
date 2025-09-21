# CharacterModel Refactor Plan - RESTART

## CRITICAL ERROR IN ORIGINAL PLAN

I completely failed to follow the explicit directions. The task was to:

**"TAKE THE _EXISTING PUBLIC-FACING API_ AND TURN THAT INTO TRAITS"**

Instead, I created completely new method signatures that don't match CharacterModel's existing API.

## CORRECT APPROACH

The traits must capture the EXACT existing public API of CharacterModel:

- `background: Var[Option[Background]]` NOT `def background: Option[Background]`
- `allPowers: Signal[List[(Power, Die)]]` NOT `def currentPowers: List[(Power, Die)]`
- `validBackground: Signal[Boolean]` NOT `def isBackgroundValid(...): Boolean`
- `changeBackground: Observer[Background]` NOT `def changeBackground(background: Background): Unit`

## STARTING OVER

1. **Step 1**: Analyze CharacterModel's ACTUAL public API
2. **Step 2**: Extract traits that match the EXACT signatures
3. **Step 3**: Make CharacterModel implement these traits with zero changes to existing code
4. **Step 4**: Only THEN extract implementations

## STATUS: COMPLETE RESTART REQUIRED