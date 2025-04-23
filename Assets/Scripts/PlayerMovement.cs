using UnityEngine;

public class PlayerMovement : MonoBehaviour
{
    //store the player's for moving horizontally
    public float speed = 5f;

    float horizontalMovement;
    float newXPositionFromUserInput;
    float leftBoundaryOfTheViewport;
    float rightBoundaryOfTheViewport;

    void Start()
    {
        Debug.Log("Hello World! Start!");
    }

   
    void Update()
    {
        MovePaddle();
    }

    void MovePaddle()
    {

        //get the horizontal input
        horizontalMovement = Input.GetAxis("Horizontal");

        /*using clamp method to limit the movement of the paddle within the viewport
         * SYNTAX: [float clampedValue = Mathf.Clamp(value, min, max)]
            value: The value you want to clamp.
            min: The minimum allowed value.
            max: The maximum allowed value.
         */

        //use the 'horizontalMovement' variable to manipulate the position of the player object
        newXPositionFromUserInput = transform.position.x + horizontalMovement * speed * Time.deltaTime;

        //get the world space boundaries of the viewport
        leftBoundaryOfTheViewport = Camera.main.ViewportToWorldPoint(new Vector3(0, 0, 0)).x;
        rightBoundaryOfTheViewport = Camera.main.ViewportToWorldPoint(new Vector3(1, 0, 0)).x;

        //clamp the 'newXPositionFromUserInput' to stay within the viewport
        newXPositionFromUserInput = Mathf.Clamp(newXPositionFromUserInput, leftBoundaryOfTheViewport, rightBoundaryOfTheViewport);

        //apply the clamped position for player 'paddle' movement
        transform.position = new Vector3(newXPositionFromUserInput, transform.position.y, transform.position.z);

    }
}
